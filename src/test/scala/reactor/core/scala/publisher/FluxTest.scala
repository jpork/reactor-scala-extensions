package reactor.core.scala.publisher

import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.reactivestreams.Subscription
import org.scalatest.FreeSpec
import reactor.core.publisher.BaseSubscriber
import reactor.test.StepVerifier

import scala.concurrent.duration.Duration

/**
  * Created by winarto on 1/10/17.
  */
class FluxTest extends FreeSpec {
  "Flux" - {
    ".combineLatest" - {
      "with combinator and sources should produce latest elements into a single element" in {
        val flux = Flux.combineLatest[Int, String]((array: Array[AnyRef]) => s"${array(0).toString}-${array(1).toString}", Mono.just(1), Mono.just(2))
        StepVerifier.create(flux)
          .expectNext("1-2")
          .verifyComplete()
      }
      "with combinator, prefetch and sources should produce latest elements into a single element" in {
        val flux = Flux.combineLatest[Int, String]((array: Array[AnyRef]) => s"${array(0).toString}-${array(1).toString}", 2, Flux.just(1, 2), Flux.just(10, 20))
        StepVerifier.create(flux)
          .expectNext("2-10")
          .expectNext("2-20")
          .verifyComplete()
      }
      "with source1, source2 and combinator should produce latest elements into a single element" in {
        val flux = Flux.combineLatest(Mono.just(1), Mono.just("a"),(int: Int, string: String) => s"${int.toString}-$string")
        StepVerifier.create(flux)
          .expectNext("1-a")
          .verifyComplete()
      }
      "with source1, source2, source3 and combinator should produce latest elements into a single element" in {
        val flux = Flux.combineLatest(Mono.just(1), Mono.just("a"), Mono.just(BigDecimal("0")),
          (array: Array[AnyRef]) => s"${array(0).toString}-${array(1).toString}-${array(2).toString}")
        StepVerifier.create(flux)
          .expectNext("1-a-0")
          .verifyComplete()
      }
      "with source1, source2, source3, source4 and combinator should produce latest elements into a single element" in {
        val flux = Flux.combineLatest(Mono.just(1), Mono.just("a"), Mono.just(BigDecimal("0")), Mono.just(1L),
          (array: Array[AnyRef]) => s"${array(0).toString}-${array(1).toString}-${array(2).toString}-${array(3).toString}")
        StepVerifier.create(flux)
          .expectNext("1-a-0-1")
          .verifyComplete()
      }
    }

    ".just" - {
      "with varargs should emit values from provided data" in {
        val flux = Flux.just(1, 2)
        StepVerifier.create(flux)
          .expectNext(1, 2)
          .verifyComplete()
      }
    }

    ".count should return Mono which emit the number of value in this flux" in {
      val mono = Flux.just(10, 9, 8).count()
      StepVerifier.create(mono)
        .expectNext(3)
        .verifyComplete()
    }

    ".map should map the type of Flux from T to R" in {
      val flux = Flux.just(1, 2, 3).map(_.toString)

      StepVerifier.create(flux)
        .expectNext("1", "2", "3")
        .expectComplete()
        .verify()
    }

    ".take should emit only n values" in {
      val flux = Flux.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).take(3)
      StepVerifier.create(flux)
        .expectNext(1, 2, 3)
        .verifyComplete()
    }

    ".sample should emit the last value for given interval" ignore {
      val flux = Flux.just(1L).sample(Duration(1, "second"))
      val counter = new CountDownLatch(3)
      flux.subscribe(new BaseSubscriber[Long] {
        override def hookOnSubscribe(subscription: Subscription): Unit = subscription.request(Long.MaxValue)

        override def hookOnNext(value: Long): Unit = {
          counter.countDown()
          Console.out.println(value)
        }
      })
      counter.await(4, TimeUnit.SECONDS)
   }
  }
}
