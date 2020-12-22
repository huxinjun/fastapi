### 版本说明:

* __1.8.6__
  Segment支持getData方法,可以获取到关联的数据等信息
* __1.8.5__
    Segment自动注入view,新增@BindAuto,@BindRoot,@OnClick注解
* __1.8.4__
    解决了模板复用导致的view引用问题
    现在segment只支持生命周期方式,旧版的set callback被删除



----

### FastApi使用步骤:

##### 1. gradle配置

   ```groovy
   implementation 'org.pulp:fastapi:1.8.5'
   ```

##### 2. 实现ApiSetting

```java

@SuppressWarnings("JavaDoc")
public class ApiSetting implements Setting {

  //重写方法
  ...

}
```

方法说明:

| 方法名                  | 解释                                                         |
| ----------------------- | ------------------------------------------------------------ |
| onGetApplicationContext | 务必传入Application实例                                      |
| onGetCacheDir           | 请求数据的缓存目录                                           |
| onGetCacheSize          | 缓存大小,单位字节,如果为0,默认10M                            |
| onGetBaseUrl            | 基础地址,不可夹带路径                                        |
| onGetPathConverter      | 将path转为url <br/>比如API上申明的是GET("ABC")<br/>两种情况不会转换:<br/>         1.GET("http://xxx"),以http开始的path<br/>         2.GET("/xxx"),以/开始的<br/>其他情况:<br/>         "ABC"会被fastapi<br/>         优先传入API Method上声明的PathConverter   <br/>         如果没有声明,会传入到Setting.onGetPathConverter配置PathConverter中 <br/>如果PathConverter都是null,相当于配置了relativePath会和BaseUrl组装为一个完整url |
| onCustomParse           | 全局自定义解析,也可使用@OnCustomParse配置在Api方法或类上,全局的优先级最低,优先级:方法注解>API声明类注解>ApiSetting重写的onCustomParse |
| onBeforeParse           | 全局解析数据前,也可使用@OnBeforeParse配置在Api方法或类上,全局的优先级最低,优先级:方法注解>API声明类注解>ApiSetting重写的OnBeforeParse |
| onErrorParse            | 全局错误解析,也可使用@OnErrorParse配置在Api方法或类上,全局的优先级最低,优先级:方法注解>API声明类注解>ApiSetting重写的OnErrorParse |
| onAfterParse            | 全局解析后,也可使用@OnAfterParse配置在Api方法或类上,全局的优先级最低,优先级:方法注解>API声明类注解>ApiSetting重写的OnAfterParse |
| onGetCommonParams       | 通用参数                                                     |
| onCustomLogger          | 自定义日志                                                   |
| onCustomLoggerLevel     | 自定义日志级别                                               |
| onGetPageCondition      | 通用分页,也可使用@Page配置在Api方法上                        |
| onGetConnectTimeout     | 网络超时时长                                                 |
| onGetReadTimeout        | 读取超时时长                                                 |
| onToastError            | 自定义Toast,框架内部分页需要弹出Toast,但是文字是英文,在此处可拦截框架中所有的Toast并进行文字转换,也可不弹出,或根据debug弹出等等 |



##### 3. 初始化

		//在使用前需要初始化,建议将下面代码放在Application的onCreate(需要保证有读取磁盘的权限)
		API.init(new ApiSetting());

##### 4. 声明一个API方法

   ```kotlin
   interface TestApi{
   		@POST("path")
   		@OnBeforeParse(ListBeforeParser::class)
   		fun getListData(@Query("testp") p: String): SimpleListObservable<ListModel>
   }
   ```

   

##### 5. 创建一个请求

   ```java
   API.get(this, TestAPI.class)
     			.getListData("method test param")
     			.success(str -> {
               output("on success callback:\n");
               output(((TextView) view).getText() + "\n");
               output(str + "\n");
               output("-----------------");
           }).faild(error -> {
               output("on error callback:\n");
               output(((TextView) view).getText() + "\n");
               output(error.getMsg() + "\n");
               output("-----------------\n");
           })
     			.toastError()
     			.lookTimeUsed("list time use---");
   ```



##### 6. Api声明支持的Observable

###### 6.1 SimpleObservable

声明:

```
@GET("/path")
fun getData(): SimpleObservable<TestModel>
```

请求:

```
SimpleObservable<TestModel> data = API.get(this, TestAPI.class).getData();
        data.success(str -> {
            //数据
        }).faild(error -> {
            //错误
        });
```



###### 6.1 SimpleListObservable

内部封装分页逻辑,需要配合@Page注解,例子:

创建PageCondition实现类:

```
public class CommonPageCondition<T extends ListModel> implements PageCondition<T> {
	...
}
```



声明Api:

```
@POST("/aaa")
@Page(CommonPageCondition::class)
fun getListData(@Query("testp") p: String): SimpleListObservable<ListModel>
```

请求:

```
API.get(this, TestAPI.class)
	.getListData("method test param")
	.nextPage()
	.success{
		//获取到的数据
	}
```

支持的方法:nextPage,prePage,page

###### 6.2 SequenceObservable

```
		@GET("getConfig")
    @MultiPath(
            "https://...",
            "http://...",
            "http://..."
    )
    fun getConfig(): SequenceObservable<UrlKey>
```

支持多Url顺序请求,直到成功返回数据

###### 6.4 URL

```kotlin
@BaseUrl("http://...")
@GET("/path/image")
fun getStaticUrl(@Query("tag") tag: String): URL
```

支持静态URL拼装





##### 7. fastapi支持的注解



| 注解           | 作用                     | 可配置位置               |
| -------------- | ------------------------ | ------------------------ |
| @BaseUrl       | 基础地址                 | Api方法,Api类            |
| @Cache         | 缓存策略                 | Api方法,Api类,ApiSetting |
| @MultiPath     | 多地址顺序访问, 直到成功 | Api方法                  |
| @OnErrorParse  | 解析错误码               | Api方法,Api类,ApiSetting |
| @OnBeforeParse | 解析前                   | Api方法,Api类,ApiSetting |
| @OnAfterParse  | 解析后                   | Api方法,Api类,ApiSetting |
| @OnCustomParse | 手动解析                 | Api方法,Api类,ApiSetting |
| @Page          | 自定义分页               | Api类,ApiSetting         |
| @Params        | 静态参数数组             | Api方法                  |
| @Param         | 静态参数                 | Api方法                  |
| @PathParser    | path转换                 | Api方法,ApiSetting       |


---

### ViewDsl使用

##### 1.例子

```kotlin
rcv_contact?.run {
  
            templete {

                type {
                    when (this.data) {
                        is Dept -> 1
                        is User -> 2
                        else -> 3
                    }
                }

                span {
                    when (this) {
                        1 -> 4
                        2 -> 1
                        else -> 0
                    }

                }

                item(1) {
                    DeptSegment::class.java.withArgs(presenter)
                }
                item(2) {
                    UserSegmentForGlass::class.java
                }
              
              	header{
                  Header1::class.java
                }
              	footer{
                  Footer1::class.java
                }

            }
            layoutManager = GridLayoutManager(context, 4)


            addItemDecoration(object : ItemDecoration() {
                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                    super.getItemOffsets(outRect, view, parent, state)
                    outRect.top = 10.dp2px()
                    outRect.bottom = 10.dp2px()


                }
            })
        }
```





##### 2.支持的方法

###### 2.1 设置适配器

​	templete{}

​	templete上下文支持的方法有:

​		__item__:定义item

​		__header__:定义header,上下文中可配置name,后面可以使用header方法查找

​		__footer__:定义footer

​		__span__:如果是GridLayoutManager可以指定某个item占用的列数,上下文是viewtype

​		__type__:定义viewtype,上下文是TypeInfo,包含item的data和pos,需要返回一个int的viewtype

###### 2.2 设置数据

```
//item data 
var arr = arrayOf(
            Data("item", "1"),
            Data("item", "2"),
            Data("item", "3")
recyclerView?.run{
		data{arr}
}
//header data
recyclerView?.run{
		dataHeader(0,Data("item", "1"))
		dataHeader(index("headerName"),Data("item", "1"))
}
//footer data
recyclerView?.run{
		dataFooter(0,Data("item", "1"))
		dataFooter(index("footerName"),Data("item", "1"))
}
```

###### 2.3 查找header

```
header(0)
header("name")
```

###### 2.4 查找footer

```
footer(0)
footer("name")
```

###### 2.5 添加header

```
headerAdd(0){
	Header::class.java.withName("headerName")
}
```

###### 2.6 删除header

```
headerRemove(0)
headerRemove("headerName")
```



###### 2.7 添加footer

```
footerAdd(0){
	Footer::class.java.withName("footerName")
}
```

###### 2.8 删除footer

```
footerRemove(0)
footerRemove("footerName")
```

###### 2.9 查找header或footer的索引

```
recyclerView?.run{
		//查找出来的是按照调用header定义顺序的索引
		//可以传入headerName或者footerName
		val headerOrFooterIndex = index("headerName")
		
}
```




##### 4. Segment实例创建

```kotlin
//手动注入view方式
class SegItem : Segment<IT>() {
  
  	//手动注入模式时,rootView可以不用声明任何注解
  	//框架将会尝试将根布局反射到每一个未声明Bind注解的字段,当然可能会失败,但是不会有任何错误发生
  	lateinit var rootView:View

  	//手动注入模式时,如果不声明Bind注解,那么该字段很可能会是null(也有可能会是rootView)
    @Bind(R.id.tv_txt)
    lateinit var tv_txt: TextView

    override fun onCreateView() = R.layout.layout1

    @SuppressLint("SetTextI18n")
    override fun onBind(bindCtx: BindingContext<IT>) {
        super.onBind(bindCtx)
        tv_txt.text = bindCtx.data.text + bindCtx.data.data
    }
}

//自动注入view
@BindAuto
class SegItem : Segment<IT>() {
  
  	//自动注入模式时,rootView的注入必须配置@BindRoot注解
  	@BindRoot
  	lateinit var rootView:View

  	//自动注入模式时,不需要声明Bind注解,但是字段名称需要和xml中的一致,或者使用驼峰方式
  	//如果未找到此view,将不产生任何的错误,但是在使用该view时将会报错
    lateinit var tv_txt: TextView

    override fun onCreateView() = R.layout.layout1

    @SuppressLint("SetTextI18n")
    override fun onBind(bindCtx: BindingContext<IT>) {
        super.onBind(bindCtx)
        tv_txt.text = bindCtx.data.text + bindCtx.data.data
    }
}
```

##### 5. Segment生命周期

1. __onReceiveArg(args: Array<out Any>)__

   在调用templete的item方法配置item所关联的segment class时,可以使用在class上使用withName或者withArgs方法,当segment实例被创建时会首先调用onReceiveArg并传入withArgs携带的参数,参数支持任意类型任意数量,需要在onReceiveArg对参数进行类型转换,并严格对应顺数

2. __onCreateView()__

   返回一个layout的id表明该segment使用的布局

3. __onViewCreated(view: View)__

   布局被创建后

4. __onBind(bindCtx: BindingContext<Data>)__

   布局数据需要更新时




##### 6. Segment支持的注解

| 注解      | 作用                 | 可配置位置  |
| --------- | -------------------- | ----------- |
| @Bind     | 绑定View,需要view id | Segment字段 |
| @BindAuto | 自动搜索并绑定       | Segment类   |
| @BindRoot | 绑定根布局           | Segment字段 |
| @OnClick  | 绑定点击事件         | Segment字段 |

__注入view注解优先级:__

> __@Bind__>__@BindRoot__>__@BindAuto__

__@OnClick说明:__

> 参数:
>
>  __value__		方法名称,可为空,为空时需要声明该view字段的类实现View.OnClickListener
>
> __interval__	点击间隔,单位:毫秒





