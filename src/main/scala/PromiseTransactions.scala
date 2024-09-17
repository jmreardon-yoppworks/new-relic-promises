
import com.newrelic.api.agent.NewRelic
import com.newrelic.api.agent.Token
import com.newrelic.api.agent.Trace
import com.newrelic.scala.api.TraceOps._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.concurrent.Promise

object PromiseTransactions {
  implicit val ec: ExecutionContext = ExecutionContext.global

  def main(args: Array[String]): Unit = {

    // Implementation using linkPromiseResult to restore the transaction
    val workingPromise = Promise[Unit]()
    val eventualWorking = working(workingPromise)
    workingPromise.trySuccess(())
    Await.ready(eventualWorking, 1.minute)
    println("done working")

    // Implementation without any special measures, does not work.
    val brokenPromise = Promise[Unit]()
    val eventualBroken = broken(brokenPromise)
    brokenPromise.trySuccess(())
    Await.ready(eventualBroken, 1.minute)
    println("done broken")
  }

  @Trace(dispatcher = true)
  private def working(promise: Promise[Unit]) = {
    printTransaction("root")
    val token = NewRelic.getAgent.getTransaction.getToken

    for {
      done <- asyncTrace("promise") {promise.future}
            _ <- linkPromiseResult(done, token)
      _ <- asyncTrace("future") {future}
    } yield printTransaction("yield")
  }

  @Trace(dispatcher = true)
  private def broken(promise: Promise[Unit]) = {
    printTransaction("root")

    for {
      done <- asyncTrace("promise") {promise.future}
      //      _ <- linkStreamResult(done, token)
      _ <- asyncTrace("future") {future}
    } yield printTransaction("yield")
  }

  @Trace(async = true, excludeFromTransactionTrace = true)
  private def linkPromiseResult[A](value: A, token: Token) = {
    token.linkAndExpire()
    Future {
      traceValue(value)
    }
  }

  @Trace(async = true, excludeFromTransactionTrace = true)
  private def traceValue[A](value: A) = value


  @Trace(async = true)
  def future(implicit ec: ExecutionContext) =
    Future {
      printTransaction("future")
    }

  def printTransaction(text: String) = {
    val metadata = NewRelic.getAgent.getTraceMetadata
    println(s"$text: ${Thread.currentThread().getId}; ${metadata.getTraceId}; ${metadata.getSpanId}")
  }
}
