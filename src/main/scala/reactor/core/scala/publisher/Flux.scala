package reactor.core.scala.publisher

import java.util.function.Function

import org.reactivestreams.{Publisher, Subscriber}
import reactor.core.publisher.{Flux => JFlux}
import java.lang.{Long => JLong}

/**
  * Created by winarto on 1/4/17.
  */
class Flux[T](jFlux: JFlux[T]) extends Publisher[T] {
  override def subscribe(s: Subscriber[_ >: T]): Unit = jFlux.subscribe(s)

  def count(): Mono[Long] = {
    new Mono[Long](jFlux.count().map(new Function[JLong, Long] {
      override def apply(t: JLong): Long = Long2long(t)
    }))
  }
}

object Flux {
  def from[T](source: Publisher[_ <: T]): Flux[T] = {
    new Flux[T](
      JFlux.from(source)
    )
  }

  def just[T](data: T*): Flux[T] = {
    new Flux[T](
      JFlux.just(data:_*)
    )
  }
}
