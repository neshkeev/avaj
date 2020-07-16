import Coroutine

import Control.Monad.Cont

printOne :: String -> CoroutineT r (Writer String) ()
printOne n = do
    lift $ tell n

fib :: Int -> Int -> CoroutineT r (Writer String) ()
fib 144 _ = yield
fib a b = do
    printOne $ show a
    yield
    fib b (a + b)

sep s = do
  printOne s
  yield

ex delim = runCoroutineT $ do
  fork $ replicateM_ 12 $ sep delim
  fib 1 1
