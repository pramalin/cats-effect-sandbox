package tutorial

import java.io._

import cats.effect._
import cats.effect.concurrent.Semaphore
import cats.implicits._
/*
Exercises: improving our small IO program
To finalize we propose you some exercises that will help you to keep improving your IO-kungfu:

  1. Modify the IOApp so it shows an error and abort the execution if the origin and destination files are the same,
   the origin file cannot be open for reading or the destination file cannot be opened for writing.
    Also, if the destination file already exists, the program should ask for confirmation before overwriting that file.

  2. Modify transmit so the buffer size is not hardcoded but passed as parameter.
  3. Use some other concurrency tool of cats-effect instead of semaphore to ensure mutual exclusion of transfer
   execution and streams closing.
  4. Create a new program able to copy folders. If the origin folder has subfolders, then their contents must be
   ecursively copied too. Of course the copying must be safely cancelable at any moment.
 */
object exercises_copy_file extends IOApp {

  def transmit[F[_]: Sync](origin: InputStream, destination: OutputStream, buffer: Array[Byte], acc: Long): F[Long] =
    for {
      amount <- Sync[F].delay(origin.read(buffer, 0, buffer.size))
      count  <- if(amount > -1) Sync[F].delay(destination.write(buffer, 0, amount)) >> transmit(origin, destination, buffer, acc + amount)
      else Sync[F].pure(acc) // End of read stream reached (by java.io.InputStream contract), nothing to write
    } yield count // Returns the actual amount of bytes transmitted

  def transfer[F[_]: Sync](origin: InputStream, destination: OutputStream): F[Long] =
    for {
      buffer <- Sync[F].delay( new Array[Byte](1024 * 10) ) // Allocated only when F is evaluated
      total  <- transmit(origin, destination, buffer, 0L)
    } yield total

  def inputStream[F[_]: Sync](f: File, guard: Semaphore[F]): Resource[F, FileInputStream] =
    Resource.make {
      Sync[F].delay(new FileInputStream(f))
    } { inStream =>
      guard.withPermit {
        Sync[F].delay(inStream.close()).handleErrorWith(_ => Sync[F].unit)
      }
    }

  def outputStream[F[_]: Sync](f: File, guard: Semaphore[F]): Resource[F, FileOutputStream] =
    Resource.make {
      Sync[F].delay(new FileOutputStream(f))
    } { outStream =>
      guard.withPermit {
        Sync[F].delay(outStream.close()).handleErrorWith(_ => Sync[F].unit)
      }
    }

  def inputOutputStreams[F[_]: Sync](in: File, out: File, guard: Semaphore[F]): Resource[F, (InputStream, OutputStream)] =
    for {
      inStream  <- inputStream(in, guard)
      outStream <- outputStream(out, guard)
    } yield (inStream, outStream)

  def copy[F[_]: Concurrent](origin: File, destination: File): F[Long] =
    for {
      guard <- Semaphore[F](1)
      count <- inputOutputStreams(origin, destination, guard).use { case (in, out) =>
        guard.withPermit(transfer(in, out))
      }
    } yield count

  val readLine = IO(scala.io.StdIn.readLine)

  // The 'main' function of IOApp //
  override def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- IO(println("Cats File copy"))
      _      <- if(args.length < 2) IO.raiseError(new IllegalArgumentException("Need origin and destination files"))
                  else IO.unit
      originName = args(0)
      destName = args(1)
      _  <- if (originName.equals(destName))
                IO(println("Source and the destination files are the same.")).as(ExitCode(2))
            else IO.unit
      orig = new File(originName)
      dest = new File(destName)
      _    <- if (dest.exists) {
                    IO(println("Destination file exists. Do you want to overwrite? [y/n]"))
                  }
                  else IO.unit
      resp  <- if (dest.exists) readLine.map(_.toString)
                else IO.pure("Y")

      _       <- if(resp.toUpperCase != "Y") IO(println("Aborted")).as(ExitCode(2))
                  else IO.unit
      count <- copy[IO](orig, dest)

      _     <- IO(println(s"$count bytes copied from ${orig.getPath} to ${dest.getPath}"))
    } yield ExitCode.Success

}