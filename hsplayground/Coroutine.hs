{-# LANGUAGE BangPatterns #-}
{-# LANGUAGE GeneralizedNewtypeDeriving #-}

module Coroutine where

import Control.Applicative
import Control.Monad.Cont (MonadCont, callCC)

import Control.Monad.Writer (MonadTrans, lift, replicateM_)
import Control.Monad (ap, liftM)

newtype StateT s m a = StateT { runStateT :: s -> m (a,s) }

instance Monad m => Monad (StateT s m) where
    return a = StateT $ \s -> return (a, s)
    m >>= k  = StateT $ \s -> do
        ~(a, s') <- runStateT m s
        runStateT (k a) s'

instance Monad m => Functor (StateT s m) where
    fmap = liftM

instance Monad m => Applicative (StateT s m) where
    pure  = return
    (<*>) = ap

instance MonadTrans (StateT s) where
  lift m = StateT $ \s -> do { x <- m ; return (x, s); }

get :: Monad m => StateT s m s
get = StateT $ \s -> return (s, s)

put :: Monad m => s -> StateT s m ()
put s = StateT $ \_ -> return ((), s)

evalStateT :: Monad m => StateT s m a -> s -> m a 
evalStateT x = fmap fst . runStateT x

newtype ContT r m a = ContT { runContT :: (a -> m r) -> m r }

instance Monad m => Monad (ContT r m) where
    return a = ContT $ \c -> do { x <- return a ; c x }
    ContT v >>= k  = ContT $ \c -> v (\a -> runContT (k a) c)

instance MonadTrans (ContT r) where
  lift m = ContT $ \c -> do { x <- m ; c x; }

instance Monad m => Functor (ContT a m) where
    fmap = liftM

instance Monad m => Applicative (ContT a m) where
    pure = return
    (<*>) = ap

instance Monad m => MonadCont (ContT r m) where
    callCC f = ContT $ \ar -> runContT (f (\a -> ContT (\br -> ar a))) ar

newtype Writer s a = Writer { runWriter :: (a, s) } deriving Show

instance Monoid s => Monad (Writer s) where
    return a = Writer (a, mempty)
    Writer (a, s) >>= k = let Writer (b, s') = k a in let !res = s <> s' in Writer (b, (seq () res))

instance Monoid s => Functor (Writer s) where
    fmap = liftM

instance Monoid s => Applicative (Writer s) where
    pure = return
    (<*>) = ap

tell :: Monoid s => s -> Writer s ()
tell s = Writer ((), s)

-- The CoroutineT monad is just ContT stacked with a StateT containing the suspended coroutines.
newtype CoroutineT r m a = CoroutineT {runCoroutineT' :: ContT r (StateT [CoroutineT r m ()] m) a}
    deriving (Functor,Applicative,Monad,MonadCont)

instance MonadTrans (CoroutineT r) where
    lift = CoroutineT . lift . lift

-- Used to manipulate the coroutine queue.
getCCs :: Monad m => CoroutineT r m [CoroutineT r m ()]
getCCs = CoroutineT $ lift get

putCCs :: Monad m => [CoroutineT r m ()] -> CoroutineT r m ()
putCCs ps = CoroutineT (lift (put ps))

-- Pop and push coroutines to the queue.
dequeue :: Monad m => CoroutineT r m ()
dequeue = do
    current_ccs <- getCCs
    case current_ccs of
        [] -> return ()
        (p:ps) -> do
            putCCs ps
            p

queue :: Monad m => CoroutineT r m () -> CoroutineT r m ()
queue p = do
    ccs <- getCCs
    putCCs (ccs++[p])

-- The interface.
yield :: Monad m => CoroutineT r m ()
yield = callCC $ \k -> do
    queue (k ())
    dequeue

fork :: Monad m => CoroutineT r m () -> CoroutineT r m ()
fork !p = callCC $ \k -> do
    queue (k ())
    p
    dequeue

-- Exhaust passes control to suspended coroutines repeatedly until there isn't any left.
exhaust :: Monad m => CoroutineT r m ()
exhaust = do
    current_ccs <- getCCs
    case null current_ccs of
        True -> return ()
        _ -> yield >>= (\_ -> exhaust)
        
-- Runs the coroutines in the base monad.
runCoroutineT :: Monad m => CoroutineT r m r -> m r
runCoroutineT !a = stateToInternal (corToState (runCoroutineT' (addExhaust a)))
  where
    stateToInternal :: Monad m => StateT [CoroutineT r m ()] m r -> m r
    stateToInternal !a = flip evalStateT [] a
    corToState :: Monad m => ContT r (StateT [CoroutineT r m ()] m) r -> StateT [CoroutineT r m ()] m r
    corToState !a = flip runContT return a
    addExhaust :: Monad m => CoroutineT r m a -> CoroutineT r m a
    addExhaust x = do { l <- x ; exhaust ; return l }

{-
printOne :: (Show a, Enum a) => a -> CoroutineT r (Writer String) ()
printOne n = do
    lift (tell $ show n)
    yield

myreplicateM_ n !e = replicateM_ n e

example = runCoroutineT $ do
    fork $ myreplicateM_ 3 (printOne 3)
    fork $ myreplicateM_ 14 (printOne 4)
    replicateM_ 2 (printOne 2)

ex = runCoroutineT $ do
    fork $ myreplicateM_ 3 (printOne 4)
    printOne 2

ex1 = runCoroutineT $ do
    fork $ myreplicateM_ 2 (printOne 4)
-}
