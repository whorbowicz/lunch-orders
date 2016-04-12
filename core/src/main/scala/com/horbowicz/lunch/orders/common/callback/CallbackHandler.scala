package com.horbowicz.lunch.orders.common.callback

sealed trait CallbackHandler[+T] extends (Callback[T] => Unit)

case class FixedValueHandler[T](responseValue: T)
  extends CallbackHandler[T] {

  override def apply(callback: Callback[T]): Unit = callback(responseValue)
}

case class FunctionWrapperHandler[T](f: Callback[T] => Unit)
  extends CallbackHandler[T] {

  override def apply(callback: Callback[T]): Unit = f(callback)
}
