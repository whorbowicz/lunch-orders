package com.horbowicz.lunch.orders.common

import scalaz._

package object callback {

  type Callback[T] = T => Unit

  //TODO report to JB/or fix issue  - code does not compile when
  //type CallbackHandler[A] = Callback[A] => Unit
  //is used
  implicit class CallbackHandler[+A](f: Callback[A] => Unit)
    extends (Callback[A] => Unit) {

    override def apply(cb: Callback[A]): Unit = f(cb)
  }

  implicit object CallbackHandlerMonad
    extends Functor[CallbackHandler] with Monad[CallbackHandler] {

    override def bind[A, B](fa: CallbackHandler[A])
      (f: (A) => CallbackHandler[B]): CallbackHandler[B] =
      (callback: Callback[B]) => fa(response => f(response)(callback))

    override def point[A](a: => A): CallbackHandler[A] =
      (callback: Callback[A]) => callback(a)

    override def map[A, B](fa: CallbackHandler[A])
      (f: (A) => B): CallbackHandler[B] =
      (callback: Callback[B]) => fa(response => callback(f(response)))
  }

}
