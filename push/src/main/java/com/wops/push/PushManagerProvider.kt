package com.wops.push

interface PushManagerProvider {

    fun getPushManager() : PushManager
}