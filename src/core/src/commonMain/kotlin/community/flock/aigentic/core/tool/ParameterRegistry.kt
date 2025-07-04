package community.flock.aigentic.core.tool


inline fun <reified T : Any> getParameter(): Parameter.Complex.Object {
    return when (val param = SerializerToParameter.convert<T>()) {
        is Parameter.Complex.Object -> param
        else -> error("Cannot get param: ${T::class.simpleName}")
    }
}
