package jooyung.com.joomoney_api.exception

class ApiException : RuntimeException {

    val resultCode: ResultCode
    val data: Map<String, Any?>?

    constructor() : super(ResultCode.ERR_SYSTEM.message) {
        this.resultCode = ResultCode.ERR_SYSTEM
        this.data = null
    }

    constructor(resultCode: ResultCode) : super(resultCode.message) {
        this.resultCode = resultCode
        this.data = null
    }

    constructor(resultCode: ResultCode, message: String?) : super(message ?: resultCode.message) {
        this.resultCode = resultCode
        this.data = null
    }

    constructor(resultCode: ResultCode, data: Map<String, Any?>?) : super(resultCode.message) {
        this.resultCode = resultCode
        this.data = data
    }

    constructor(resultCode: ResultCode, message: String?, data: Map<String, Any?>?) : super(message ?: resultCode.message) {
        this.resultCode = resultCode
        this.data = data
    }
}