package co.smartreceipts.push

interface PushManagerProvider {

    fun getPushManager() : PushManager
}