package com.horbowicz.lunch.orders.domain

trait IdProvider[Id]
{
  def get(): Id
}
