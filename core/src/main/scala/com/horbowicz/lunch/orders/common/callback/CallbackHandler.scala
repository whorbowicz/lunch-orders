package com.horbowicz.lunch.orders.common.callback

sealed trait CallbackHandler[+A] extends (Callback[A] => Unit)
{
  def map[B](f: A => B): CallbackHandler[B]
}

case class FixedValueHandler[A](responseValue: A)
  extends CallbackHandler[A]
{

  override def apply(callback: Callback[A]): Unit = callback(responseValue)

  override def map[B](f: A => B): CallbackHandler[B] = FixedValueHandler(
    f(
      responseValue))
}

case class FunctionWrapperHandler[A](f: Callback[A] => Unit)
  extends CallbackHandler[A]
{

  override def apply(callback: Callback[A]): Unit = f(callback)

  override def map[B](g: A => B): CallbackHandler[B] = FunctionWrapperHandler(
    (callback: Callback[B]) => f(response => callback(g(response))))
}
