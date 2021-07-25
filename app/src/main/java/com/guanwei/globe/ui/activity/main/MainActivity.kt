package com.guanwei.globe.ui.activity.main

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.view.View
import androidx.core.app.ActivityCompat
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.guanwei.globe.R
import com.guanwei.globe.data.LocationEntity
import com.guanwei.globe.data.PoiEntity
import com.guanwei.globe.databinding.ActivityMainBinding
import com.guanwei.globe.http.BaseResponse
import com.guanwei.globe.utils.GlobeEvaluator
import com.guanwei.globe.utils.toast
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.jzh.demo.mvp.base.activity.BaseActivity
import gov.nasa.worldwind.WorldWind
import gov.nasa.worldwind.WorldWindow
import gov.nasa.worldwind.geom.Position
import gov.nasa.worldwind.globe.BasicElevationCoverage
import gov.nasa.worldwind.layer.BackgroundLayer
import gov.nasa.worldwind.layer.BlueMarbleLandsatLayer
import gov.nasa.worldwind.layer.RenderableLayer
import gov.nasa.worldwind.render.Color
import gov.nasa.worldwind.shape.Polygon
import gov.nasa.worldwind.shape.ShapeAttributes
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : BaseActivity<ActivityMainBinding, MainPresenter>(), MainContract.MainView {

    private var wwd: WorldWindow? = null
    private var isClick = false
    private var isTailLocation = false

    //绘制XY坐标集合
    private var positions: ArrayList<Position> = ArrayList()

    //跟踪经纬度集合
    private var poiEntities: ArrayList<PoiEntity> = ArrayList()

    private var layerCount = 0

    private var locationManager: LocationManager? = null

    private var myLat = 0.0
    private var myLon = 0.0
    private var myAlt = 0.0

    private var selectFlag = -1

    //权限数组
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )

    override fun createPresenter(): MainPresenter {
        return MainPresenter(this)
    }

    override fun initView(): Int {
        return R.layout.activity_main
    }

    override fun initData() {

        //权限申请
        checkPermission()

        //创建地球窗口
        wwd = WorldWindow(this).apply {
            //添加图层
            layers.addLayer(BackgroundLayer())
            layers.addLayer(BlueMarbleLandsatLayer())
            globe.elevationModel.addCoverage(BasicElevationCoverage())
        }

        //获取初始化时添加了几个图层
        layerCount = wwd!!.layers.count()

        //添加地球
        globe.addView(wwd)

    }


    override fun initListener() {
        //绘制点击事件
        click_point.setOnClickListener {
            isClick = !isClick
            if (isClick) {
                click_point.text = getString(R.string.end_draw)
            } else {
                click_point.text = getString(R.string.start_draw)
            }
            globe_parent.setDraw(isClick)

        }

        //清除点击事件
        clean_point.setOnClickListener {
            globe_parent.cleanDraw()
            val layers = wwd!!.layers
            if (layers.count() > layerCount) {
                for (i in layerCount until layers.count()) {
                    layers.removeLayer(i)
                    break
                }
                wwd!!.requestRedraw()
            }
            //清除选择类型
            selectFlag = -1
        }

        //确认点击事件
        agree_btn.setOnClickListener {
            if (isClick) {
                //获取坐标集合
                val poiList = globe_parent.poiList
                if (poiList.size <= 2) {
                    toast("坐标点数量无法形成一个面")
                    return@setOnClickListener
                }
                //选择类型为绘制
                selectFlag = 1
                xyTo(poiList)
                isClick = false
                click_point.text = getString(R.string.start_draw)
                globe_parent.setDraw(isClick)
                globe_parent.cleanDraw()
            }
        }

        //地理位置监听回调
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        //开始跟踪点击事件
        start_tail_after.setOnClickListener {
            if (isClick) {
                toast("请关闭绘制")
                return@setOnClickListener
            }
            //跟踪获取经纬度
            selectFlag = 0
            if (!isTailLocation) {
                //开始跟踪点击
                isTailLocation = true
                start_tail_after.text = getString(R.string.end_tail_after)
                //同意
                startGetLocation()
            } else {
                if (poiEntities.size > 0) {
                    xyTo(poiEntities)
                } else {
                    toast("跟踪时长较短")
                }
                //结束跟踪点击
                start_tail_after.text = getString(R.string.start_tail_after)
                isTailLocation = false
                //重新初始化数据
                myLat = 0.0
                myLon = 0.0
                poiEntities.clear()
                //隐藏经纬度显示
                lat_txt.visibility = View.INVISIBLE
                lon_txt.visibility = View.INVISIBLE
            }
        }

    }

    /**
     * 将坐标转换成为经纬度
     */
    private fun xyTo(poiList: List<PoiEntity>) {
        positions.clear()
        if (selectFlag == 0) {//跟踪获取位置
            for (poi in poiList.indices) {
                //添加经纬度
                positions.add(Position(poiList[poi].x, poiList[poi].y, 5000.0))
            }
        } else if (selectFlag == 1) {//绘制获取位置
            for (i in poiList.indices) {
                val pick = wwd!!.pick(poiList[i].x.toFloat(), poiList[i].y.toFloat())
                val pickedObjectAt = pick.pickedObjectAt(0)
                //获取到经纬度
                val lat: Double = pickedObjectAt.terrainPosition.latitude
                val lon: Double = pickedObjectAt.terrainPosition.longitude
                //添加经纬度
                positions.add(Position(lat, lon, 5000.0))
            }
        }

        //设置地球显示的面
        val attrs = ShapeAttributes()
        //设置透明度
        attrs.interiorColor = Color(1f, 1f, 1f, 0.2f)
        attrs.outlineWidth = 3f
        val poly = Polygon(positions, attrs).apply {
            altitudeMode = WorldWind.CLAMP_TO_GROUND
            isFollowTerrain = true
        }

        val locateLayer = RenderableLayer("VectorTest")
        locateLayer.addRenderable(poly)
        wwd!!.layers.addLayer(locateLayer)
        wwd!!.requestRedraw()

        //转换成为geojson
        val locationEntity = LocationEntity()
        locationEntity.type = "FeatureCollection"
        locationEntity.name = "graphic"
        val crsBean = LocationEntity.CrsBean()
        crsBean.type = "name"
        val propertiesBean = LocationEntity.CrsBean.PropertiesBean()
        propertiesBean.name = "urn:ogc:def:crs:OGC:1.3:CRS84"
        crsBean.properties = propertiesBean
        locationEntity.crs = crsBean

        val featuresBeans: ArrayList<LocationEntity.FeaturesBean> = ArrayList()

        val featuresBeanX = LocationEntity.FeaturesBean()
        featuresBeanX.type = "Feature"
        val propertiesBeanX = LocationEntity.FeaturesBean.PropertiesBeanX()
        propertiesBeanX.gvtype = 2
        featuresBeanX.properties = propertiesBeanX

        val geometryBean = LocationEntity.FeaturesBean.GeometryBean()
        geometryBean.type = "POLYGON"

        val coordinates: List<List<List<Double>>> = ArrayList()
        val firstList: ArrayList<ArrayList<Double>> = ArrayList()
        for (i in positions.indices) {
            val secondList: ArrayList<Double> = ArrayList()
            secondList.add(positions[i].latitude)
            secondList.add(positions[i].longitude)
            secondList.add(positions[i].altitude)
            firstList.add(secondList)
        }
        //最后进行闭环
        if (firstList.size == positions.size) {
            val secondList: ArrayList<Double> = ArrayList()
            secondList.add(positions[0].latitude)
            secondList.add(positions[0].longitude)
            secondList.add(positions[0].altitude)
            firstList.add(secondList)
        }

        geometryBean.coordinates = coordinates

        featuresBeanX.geometry = geometryBean

        (coordinates as ArrayList).add(firstList)
        featuresBeans.add(featuresBeanX)
        locationEntity.features = featuresBeans
        //转换成json
        val json = GsonUtils.toJson(locationEntity)
        LogUtils.a("to json", json)
        //使用map添加键名
        val map = HashMap<String, String>()
        map["geoJson"] = json

        val geoJson = JSONObject(map as Map<*, *>).toString()
        //请求接口上传经纬度
        mPresenter!!.postLocation(
            geoJson.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        )
        selectFlag = -1
    }

    override fun postLocation(response: BaseResponse<Any>) {
        toast("上传成功")
    }

    override fun showLoading() {

    }

    override fun hideLoading() {

    }

    /**
     * 中心位置
     */
    private fun currentViewPosition(): Position {
        val position = Position()
        //先获取当前位置
        val lat = wwd!!.navigator.latitude//纬度
        val lon = wwd!!.navigator.longitude//经度
        val alt = wwd!!.navigator.altitude//海拔
        position.set(lat, lon, alt)
        return position
    }

    private fun moveTo() {
        val startP = currentViewPosition()
        val endP = Position(myLat, myLon, 5000.0)
        val animator = ValueAnimator.ofObject(GlobeEvaluator(), startP, endP)
        animator.duration = 5000
        animator.addUpdateListener {
            val currentPosition = animator.animatedValue as Position
            wwd!!.navigator.latitude = currentPosition.latitude
            wwd!!.navigator.longitude = currentPosition.longitude
            wwd!!.navigator.altitude = currentPosition.altitude
            wwd!!.requestRedraw()
        }
        animator.start()
    }

    /**
     * 开始采集
     */
    @SuppressLint("SetTextI18n")
    private fun startGetLocation() {
        //获取安卓自带位置服务管理
        if (locationManager == null)
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        //位置监听回调
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationManager!!.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            500L,
            0f
        ) {
            //是否开启定位
            if (isTailLocation) {
                LogUtils.a("经度为", it.latitude)
                LogUtils.a("纬度为", it.longitude)
                LogUtils.a("海拔为", it.altitude)
                lat_txt.visibility = View.VISIBLE
                lon_txt.visibility = View.VISIBLE

                //设置显示经纬度
                lat_txt.text = "经度为:${it.latitude}"
                lon_txt.text = "纬度为:${it.longitude}"

                //存储数据到集合
                poiEntities.add(PoiEntity(it.latitude, it.longitude))
                if (myLat == 0.0 && myLon == 0.0) {
                    myLat = it.latitude
                    myLon = it.longitude
                    myAlt = it.altitude
                    moveTo()
                }
            }
        }
        toast("开始位置跟踪")
    }

    /**
     * 权限检查申请
     */
    private fun checkPermission() {
        XXPermissions.with(this)
            .permission(permissions)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>?, all: Boolean) {

                }

                override fun onDenied(permissions: MutableList<String>?, never: Boolean) {
                    //拒绝
                    toast("权限拒绝，无法开始位置跟踪")
                }
            })
    }

}
