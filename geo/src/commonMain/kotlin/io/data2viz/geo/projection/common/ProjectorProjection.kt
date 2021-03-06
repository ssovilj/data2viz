/*
 * Copyright (c) 2018-2019. data2viz sàrl.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package io.data2viz.geo.projection.common

import io.data2viz.geo.geometry.clip.NoClip
import io.data2viz.geo.geometry.clip.ClipStreamBuilder
import io.data2viz.geo.geometry.clip.antimeridianPreClip
import io.data2viz.geo.stream.DelegateStreamAdapter
import io.data2viz.geo.stream.Stream
import io.data2viz.math.Angle
import io.data2viz.math.rad
import io.data2viz.math.toDegrees
import io.data2viz.math.toRadians
import kotlin.math.sqrt


/**
 * Create [Projection] for give [Projector]
 */
public fun projection(projector: Projector, init: ProjectorProjection.() -> Unit): Projection =
    ProjectorProjection(projector)
        .apply(init)


private val transformRadians: (stream: Stream) -> DelegateStreamAdapter = { stream: Stream ->
    object : DelegateStreamAdapter(stream) {
        override fun point(x: Double, y: Double, z: Double) =
            stream.point(x.toRadians(), y.toRadians(), z.toRadians())
    }
}

private fun transformRotate(rotateProjector: Projector): (stream: Stream) -> DelegateStreamAdapter = { stream: Stream ->
    object : DelegateStreamAdapter(stream) {
        override fun point(x: Double, y: Double, z: Double) {
            val projection = rotateProjector.project(x,y)
            stream.point(projection[0], projection[1], 0.0)
        }
    }
}

/**
 * Base [Projection] implementation
 * Uses [CachedProjection]
 *
 * @see Projection
 * @see ComposedProjection
 */
public open class ProjectorProjection(
    public val projector: Projector) : Projection {

    private var _translateX = 480.0
    private var _translateY = 250.0


    // Center
    private var _recenterDx = 0.0
    private var _recenterDy = 0.0

    // in radians
    private var _centerLat = 0.0
    // in radians
    private var _centerLon = 0.0

    private var _scale = 150.0

    /**
     * TODO: rework to affine matrix transformations or at least separate Scale & Translate phase
     */
    protected lateinit var composedTransformationsProjector: Projector

    protected val translateAndScaleProjector: TranslateAndScaleProjector = TranslateAndScaleProjector(projector, _scale, _recenterDx, _recenterDy)

    // Precision
    private var _precisionDelta2 = 0.5

    // Rotate
    protected var _rotationLambda: Double = 0.0
    protected var _rotationPhi: Double = 0.0
    protected var _rotationGamma: Double = 0.0
    protected lateinit var rotator: Projector

    override var preClip: ClipStreamBuilder = antimeridianPreClip

    override var postClip: ClipStreamBuilder = NoClip

    private var resampleProjector: (Stream) -> Stream = resample(translateAndScaleProjector, _precisionDelta2)

    override var scale: Double
        get() = _scale
        set(value) {
            _scale = value
            recenter()
        }


    // Translate
    override var translateX: Double
        get () = _translateX
        set(value) {
            _translateX = value
            recenter()
        }
    override var translateY: Double
        get () = _translateY
        set(value) {
            _translateY = value
            recenter()
        }


    override fun translate(x: Double, y: Double) {
        _translateX = x
        _translateY = y
        recenter()
    }


    override var centerLat: Angle
        get() = _centerLat.rad
        set(value) {
            _centerLat = value.rad
            recenter()
        }
    override var centerLon: Angle
        get() = _centerLon.rad
        set(value) {
            _centerLon = value.rad
            recenter()
        }


    override fun center(lat: Angle, lon: Angle) {
        _centerLat = lat.rad
        _centerLon = lon.rad
        recenter()
    }

    override var rotateLambda: Angle
        get() = _rotationLambda.rad
        set(value) {
            _rotationLambda = value.rad
            recenter()
        }

    override var rotatePhi: Angle
        get() = _rotationPhi.rad
        set(value) {
            _rotationPhi = value.rad
            recenter()
        }


    override var rotateGamma: Angle
        get() = _rotationGamma.rad
        set(value) {
            _rotationGamma = value.rad
            recenter()
        }


    override fun rotate(lambda: Angle, phi: Angle, gamma: Angle?) {
        _rotationLambda = lambda.rad
        _rotationPhi = phi.rad
        _rotationGamma = gamma?.rad ?: 0.0
        recenter()
    }


    override var precision: Double
        get() = sqrt(_precisionDelta2)
        set(value) {
            _precisionDelta2 = value * value
            resampleProjector = resample(translateAndScaleProjector, _precisionDelta2)
        }


    override fun bindTo(downstream: Stream): Stream {
        return transformRadians(
            transformRotate(rotator)(
                    preClip.bindTo(
                        resampleProjector(
                            postClip.bindTo(downstream)
                        )
                )
            )
        )
    }


    override fun project(lambda: Double, phi: Double): DoubleArray {
        val lambdaRadians = lambda.toRadians()
        val phiRadians = phi.toRadians()
        return composedTransformationsProjector.project(lambdaRadians, phiRadians)
    }


    override fun invert(x: Double, y: Double): DoubleArray {
        val inverted = composedTransformationsProjector.invert(x, y)
        return doubleArrayOf(
            inverted[0].toDegrees(),
            inverted[1].toDegrees()
        )
    }

    private fun recenter() {
        rotator = createRotateRadiansProjector(_rotationLambda, _rotationPhi, _rotationGamma)

        composedTransformationsProjector = ComposedProjector(rotator, translateAndScaleProjector)

        val projectedCenter = projector.project(_centerLat, _centerLon)

        _recenterDx = translateX - (projectedCenter[0] * _scale)
        _recenterDy = translateY + (projectedCenter[1] * _scale)

        translateAndScaleProjector.scale = _scale
        translateAndScaleProjector.recenterDx = _recenterDx
        translateAndScaleProjector.recenterDy = _recenterDy
    }

}

