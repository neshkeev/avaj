{-# LANGUAGE BangPatterns #-}
{-# LANGUAGE GeneralizedNewtypeDeriving #-}
-- We use GeneralizedNewtypeDeriving to avoid boilerplate. As of GHC 7.8, it is safe.

import Control.Applicative
import Control.Monad.Cont
import Control.Monad.State

-- The CoroutineT monad is just ContT stacked with a StateT containing the suspended coroutines.
newtype CoroutineT r m a = CoroutineT {runCoroutineT' :: ContT r (StateT [CoroutineT r m ()] m) a}
    deriving (Functor,Applicative,Monad,MonadCont,MonadIO)

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
fork p = callCC $ \k -> do
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
runCoroutineT a = one (two (runCoroutineT' (three a)))
  where
    one :: Monad m => StateT [CoroutineT r m ()] m r -> m r
    one = flip evalStateT []
    two :: Monad m => ContT r (StateT [CoroutineT r m ()] m) r -> StateT [CoroutineT r m ()] m r
    two = flip runContT return
    three :: Monad m => CoroutineT r m a -> CoroutineT r m a
    three x = eagerConst x exhaust
    eagerConst :: Monad m => CoroutineT r m a -> CoroutineT r m () -> CoroutineT r m a
    eagerConst !x !y = (liftA2 const) x y

printOne n = do
    liftIO (print n)
    yield

example = runCoroutineT $ do
    fork $ replicateM_ 3 (printOne 3)
    fork $ replicateM_ 14 (printOne 4)
    replicateM_ 2 (printOne 2)

main = example
