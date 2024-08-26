package community.flock.aigentic.platform.testing.util

const val ANSI_RESET = "\u001B[0m"
const val ANSI_RED = "\u001B[31m"
const val ANSI_GREEN = "\u001B[32m"

fun Any.greenString() = "$ANSI_GREEN$this$ANSI_RESET"

fun Any.redString() = "$ANSI_RED$this$ANSI_RESET"
