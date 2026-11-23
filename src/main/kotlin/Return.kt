// A control-flow signal, not a runtime error. Only the calling protocol catches it.
class Return(val value: Any?) : RuntimeException(null, null, false, false)
