package com.gmail.alsubbota.liveevent

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class InstantTaskExecutorExtension: AfterEachCallback, BeforeEachCallback {
    override fun beforeEach(context: ExtensionContext?) {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) {
                runnable.run()
            }
            override fun isMainThread(): Boolean {
                return true
            }
            override fun postToMainThread(runnable: Runnable) {
                runnable.run()
            }
        })
    }

    override fun afterEach(context: ExtensionContext?) {
        ArchTaskExecutor.getInstance().setDelegate(null)
    }
}