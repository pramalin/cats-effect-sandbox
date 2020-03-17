package io_monad_for_cats

import cats.effect._



object example5_shift {
  trait Response[T] {
    def onError(t: Throwable): Unit
    def onSuccess(t: T): Unit
  }
  // defined trait Response

  trait Channel {
    def sendBytes(chunk: Array[Byte], handler: Response[Unit]): Unit
    def receiveBytes(handler: Response[Array[Byte]]): Unit
  }
  // defined trait Channel

  def send(c: Channel, chunk: Array[Byte]): IO[Unit] = {
    IO async { cb =>
      c.sendBytes(chunk, new Response[Unit] {
        def onError(t: Throwable) = cb(Left(t))
        def onSuccess(v: Unit) = cb(Right(()))
      })
    }
  }
  // send: (c: Channel, chunk: Array[Byte])cats.effect.IO[Unit]

  def receive(c: Channel): IO[Array[Byte]] = {
    IO async { cb =>
      c.receiveBytes(new Response[Array[Byte]] {
        def onError(t: Throwable) = cb(Left(t))
        def onSuccess(chunk: Array[Byte]) = cb(Right(chunk))
      })
    }
  }
  // receive: (c: Channel)cats.effect.IO[Array[Byte]]

  val c: Channel = null // pretend this is an actual channel

  import scala.concurrent._
  implicit val ec = ExecutionContext.global

  for {
    _ <- send(c, "SYN".getBytes)
    response <- (receive(c)).shift    // there's no place like home!
    _ <- if (response.equals("ACK".getBytes))   // pretend == works on Array[Byte]
      IO { println("found the guy!") }
    else
      IO { println("no idea what happened, but it wasn't good") }
  } yield ()

}
