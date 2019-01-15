// https://typelevel.org/blog/2017/05/02/io-monad-for-cats.html

import cats.effect.IO

def putStrLn(line: String): IO[Unit] =
  IO { println(line) }

def f(a: IO[Unit], b: IO[Unit]): Unit = {
  a.unsafeRunSync
  b.unsafeRunSync
}

f(putStrLn("hi!"), putStrLn("hi!"))

// is equivalent to

val x = putStrLn("hi!")
f(x, x)


