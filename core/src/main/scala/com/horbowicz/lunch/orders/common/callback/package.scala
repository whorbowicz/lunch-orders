package com.horbowicz.lunch.orders.common

package object callback {

  type Callback[T] = T => Unit

  implicit class FromValue[T](value: T) {

    def response: CallbackHandler[T] = new FixedValueHandler[T](value)
  }

  implicit class FromFunction[T](f: Callback[T] => Unit) {

    def callbackHandler: CallbackHandler[T] = new FunctionWrapperHandler[T](f)
  }

}
