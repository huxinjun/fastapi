package cn.xzbenben.test

class i {}

inline fun inject(data: Any, init: () -> Unit) {

}

inline fun i.bind(init: () -> Unit) {

}


//class bind(r: Int, d: Any) : (Int, Any) -> Unit {
//    override fun invoke(resId: Int, data: Any) {
//
//    }
//
//}

//class inject(data: Any) : () -> Unit {
//
//    var data: Any = data
//
//    override fun invoke() {
//    }
//
//}

class Data(n: String) {
    var name: String = n
}


//class bind : (Int, Any) -> bind {
//    var viewId: Int = 0
//    var data: Any? = null
//
//    override fun invoke(id: Int, data: Any): bind {
//        val bind = bind()
//        bind.viewId = id
//        return bind
//    }
//
//}


fun main() {

    val data = Data("dadada")

//    b{
//        d=xxx
//        v=xxx
//
//        b(d,v)
//
//        b{
//
//        }
//
//    }


    //        bind(1, "")
//        bind(R.id.rcv, list)
//        bind {
//            view = xxx
//            data = xxx
//            item = { type, pos, data -> {} }
//        }

}
