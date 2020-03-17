import cats.effect.IO

val boom = IO.raiseError(new Exception("boom"))
boom.attempt.unsafeRunSync()