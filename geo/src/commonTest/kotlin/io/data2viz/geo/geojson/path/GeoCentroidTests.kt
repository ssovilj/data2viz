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

package io.data2viz.geo.geojson.path


import io.data2viz.geo.GeoPoint
import io.data2viz.geo.geojson.Sphere
import io.data2viz.geo.projection.pt
import io.data2viz.geojson.*
import io.data2viz.math.deg
import io.data2viz.test.TestBase
import kotlin.test.Test

class GeoCentroidTests : TestBase() {

    @Test
    fun geocentroid_of_a_point_is_itself() {
        centroid(Point(pt(.0, .0))) shouldBeClose target(.0, .0)
        centroid(Point(pt(1.0, 1.0))) shouldBeClose target(1.0, 1.0)
        centroid(Point(pt(2.0, 3.0))) shouldBeClose target(2.0, 3.0)
        centroid(Point(pt(-4.0, -5.0))) shouldBeClose target(-4.0, -5.0)
    }

    @Test
    fun geocentroid_of_a_set_of_points_is_the_spherical_average_of_its_constituent_members() {
        centroid(
            MultiPoint(
                arrayOf(
                    pt(.0, .0),
                    pt(1.0, 2.0)
                )
            )
        ) shouldBeClose target(0.499847, 1.000038)

        centroid(
            MultiPoint(
                arrayOf(
                    pt(179.0, .0),
                    pt(-179.0, .0)
                )
            )
        ) shouldBeClose target(180.0, .0)
    }

    @Test
    fun geocentroid_of_a_set_of_points_and_their_antipodes_is_ambiguous() {
        centroid(
            MultiPoint(
                arrayOf(
                    pt(.0, .0),
                    pt(180.0, .0)
                )
            )
        ) shouldBe null

        centroid(
            MultiPoint(
                arrayOf(
                    pt(.0, .0),
                    pt(90.0, .0),
                    pt(180.0, .0),
                    pt(-90.0, .0)
                )
            )
        ) shouldBe null

        centroid(
            MultiPoint(
                arrayOf(
                    pt(.0, .0),
                    pt(.0, 90.0),
                    pt(180.0, .0),
                    pt(.0, -90.0)
                )
            )
        ) shouldBe null
    }

    @Test
    fun geocentroid_of_an_empty_set_of_points_is_ambiguous() {
        centroid(MultiPoint(arrayOf())) shouldBe null
    }

    @Test
    fun geocentroid_of_a_linestring_is_the_spherical_average_of_its_constituent_great_arc_segments() {
        centroid(
            LineString(
                arrayOf(
                    pt(.0, .0),
                    pt(1.0, .0)
                )
            )
        ) shouldBeClose target(0.5, .0)

        centroid(
            LineString(
                arrayOf(
                    pt(.0, .0),
                    pt(.0, 90.0)
                )
            )
        ) shouldBeClose target(.0, 45.0)

        centroid(
            LineString(
                arrayOf(
                    pt(.0, .0),
                    pt(.0, 45.0),
                    pt(.0, 90.0)
                )
            )
        ) shouldBeClose target(.0, 45.0)

        centroid(
            LineString(
                arrayOf(
                    pt(-1.0, -1.0),
                    pt(1.0, 1.0)
                )
            )
        ) shouldBeClose target(.0, .0)

        centroid(
            LineString(
                arrayOf(
                    pt(-60.0, -1.0),
                    pt(60.0, 1.0)
                )
            )
        ) shouldBeClose target(.0, .0)

        centroid(
            LineString(
                arrayOf(
                    pt(179.0, -1.0),
                    pt(-179.0, 1.0)
                )
            )
        ) shouldBeClose target(180.0, .0)

        centroid(
            LineString(
                arrayOf(
                    pt(-179.0, .0),
                    pt(.0, .0),
                    pt(179.0, .0)
                )
            )
        ) shouldBeClose target(.0, .0)

        centroid(
            LineString(
                arrayOf(
                    pt(-180.0, -90.0),
                    pt(.0, .0),
                    pt(.0, 90.0)
                )
            )
        ) shouldBeClose target(.0, .0)
    }

    @Test
    fun geocentroid_of_a_great_arc_from_a_point_to_its_antipode_is_ambiguous() {
        centroid(
            LineString(
                arrayOf(
                    pt(180.0, .0),
                    pt(.0, .0)
                )
            )
        ) shouldBe null

        centroid(
            MultiLineString(
                arrayOf(
                    arrayOf(
                        pt(180.0, .0),
                        pt(.0, .0)
                    )
                )
            )
        ) shouldBe null
    }

    @Test
    fun geocentroid_of_a_set_of_linestrings_is_the_spherical_average_of_its_constituent_great_arc_segments() {
        centroid(
            MultiLineString(
                arrayOf(
                    arrayOf(
                        pt(.0, .0),
                        pt(.0, 2.0)
                    )
                )
            )
        ) shouldBeClose target(.0, 1.0)
    }

    @Test
    fun geocentroid_a_line_of_zero_length_is_treated_as_points() {
        centroid(
            LineString(
                arrayOf(
                    pt(1.0, 1.0),
                    pt(1.0, 1.0)
                )
            )
        ) shouldBeClose target(1.0, 1.0)

        // TODO
//        test.inDelta(d3.geoCentroid({type: "GeometryCollection", geometries: [{type: "Point", coordinates: [0, 0]}, {type: "LineString", coordinates: [[1, 2], [1, 2]]}]}), [0.666534, 1.333408], 1e-6);
    }

    @Test
    fun geocentroid_an_empty_polygon_with_non_zero_extent_is_treated_as_a_line() {
        centroid(
            Polygon(
                arrayOf(
                    arrayOf(
                        pt(1.0, 1.0),
                        pt(2.0, 1.0),
                        pt(3.0, 1.0),
                        pt(2.0, 1.0),
                        pt(1.0, 1.0)
                    )
                )
            )
        ) shouldBeClose target(2.0, 1.000076)

        // TODO
//        test.inDelta(d3.geoCentroid({type: "GeometryCollection", geometries: [{type: "Point", coordinates: [0, 0]}, {type: "Polygon", coordinates: [[[1, 2], [1, 2], [1, 2], [1, 2]]]}]}), [0.799907, 1.600077], 1e-6);
    }

    @Test
    fun geocentroid_an_empty_polygon_with_zero_extent_is_treated_as_a_point() {
        centroid(
            Polygon(
                arrayOf(
                    arrayOf(
                        pt(1.0, 1.0),
                        pt(1.0, 1.0),
                        pt(1.0, 1.0),
                        pt(1.0, 1.0),
                        pt(1.0, 1.0)
                    )
                )
            )
        ) shouldBeClose target(1.0, 1.0)

        // TODO
//        test.inDelta(d3.geoCentroid({type: "GeometryCollection", geometries: [{type: "Point", coordinates: [0, 0]}, {type: "Polygon", coordinates: [[[1, 2], [1, 2], [1, 2], [1, 2]]]}]}), [0.799907, 1.600077], 1e-6);
    }

    @Test
    fun geocentroid_of_the_equator_is_ambiguous() {
        centroid(
            LineString(
                arrayOf(
                    pt(0.0, .0),
                    pt(120.0, .0),
                    pt(-120.0, .0),
                    pt(.0, .0)
                )
            )
        ) shouldBe null
    }

    @Test
    fun geocentroid_of_a_polygon_is_the_spherical_average_of_its_surface() {
        centroid(
            Polygon(
                arrayOf(
                    arrayOf(
                        pt(.0, -90.0),
                        pt(.0, .0),
                        pt(.0, 90.0),
                        pt(1.0, .0),
                        pt(.0, -90.0)
                    )
                )
            )
        ) shouldBeClose target(0.5, .0)

        centroid(
            Polygon(
                arrayOf(
                    (-180..180).map { pt(it.toDouble(), -60.0) }.toTypedArray()
                )
            )
        )?.lat?.deg ?: .0 shouldBeClose -90.0

        centroid(
            Polygon(
                arrayOf(
                    arrayOf(
                        pt(.0, -10.0),
                        pt(.0, 10.0),
                        pt(10.0, 10.0),
                        pt(10.0, -10.0),
                        pt(.0, -10.0)
                    )
                )
            )
        ) shouldBeClose target(5.0, .0)
    }

    @Test
    fun geocentroid_of_a_spherical_square_touching_the_antimeridian() {
        centroid(
            Polygon(
                arrayOf(
                    arrayOf(
                        pt(-180.0, .0),
                        pt(-180.0, 10.0),
                        pt(-179.0, 10.0),
                        pt(-179.0, .0),
                        pt(-180.0, .0)
                    )
                )
            )
        ) shouldBeClose target(-179.5, 4.987448)
    }

    @Test
    fun geocentroid_of_a_sphere_is_ambiguous() {
        centroid(Sphere()) shouldBe null
    }

    @Test
    fun geocentroid_of_a_small_circle_is_its_center_south_pole() {
        centroid(
            Polygon(
                arrayOf(
                    (-180..180).map { pt(it.toDouble(), -60.0) }.toTypedArray()
                )
            )
        )?.lat?.deg ?: .0 shouldBeClose -90.0
    }

    @Test
    fun geocentroid_of_a_small_circle_is_its_center_equator() {
        val geo: GeoJsonObject = Polygon(
            arrayOf(
                arrayOf(
                    pt(.0, -10.0),
                    pt(.0, 10.0),
                    pt(10.0, 10.0),
                    pt(10.0, -10.0),
                    pt(.0, -10.0)
                )
            )
        )
        centroid(geo) shouldBeClose target(5.0, .0)
    }

    @Test
    fun geocentroid_of_a_small_circle_is_its_center_equator_with_coincident_points() {
        centroid(
            Polygon(
                arrayOf(
                    arrayOf(
                        pt(.0, -10.0),
                        pt(.0, 10.0),
                        pt(.0, 10.0),
                        pt(10.0, 10.0),
                        pt(10.0, -10.0),
                        pt(.0, -10.0)
                    )
                )
            )
        ) shouldBeClose target(5.0, .0)
    }

    @Test
    fun geocentroid_of_a_small_circle_is_its_center_other() {
        centroid(
            Polygon(
                arrayOf(
                    arrayOf(
                        pt(-180.0, .0),
                        pt(-180.0, 10.0),
                        pt(-179.0, 10.0),
                        pt(-179.0, .0),
                        pt(-180.0, .0)
                    )
                )
            )
        ) shouldBeClose target(-179.5, 4.987448)
    }

    @Test
    fun geocentroid_of_a_feature_is_the_center_of_its_constituent_geometry() {
        centroid(
            Feature(
                LineString(
                    arrayOf(
                        pt(1.0, 1.0),
                        pt(1.0, 1.0)
                    )
                )
            )
        ) shouldBeClose target(1.0, 1.0)

        centroid(
            Feature(
                Point(
                    pt(1.0, 1.0)
                )
            )
        ) shouldBeClose target(1.0, 1.0)

        centroid(
            Feature(
                Polygon(
                    arrayOf(
                        arrayOf(
                            pt(.0, -90.0),
                            pt(.0, .0),
                            pt(.0, 90.0),
                            pt(1.0, .0),
                            pt(.0, -90.0)
                        )
                    )
                )
            )
        ) shouldBeClose target(.5, .0)
    }

    @Test
    fun geocentroid_of_a_featureCollection_is_the_center_of_its_constituent_geometry() {
        centroid(
            FeatureCollection(
                arrayOf(
                    Feature(
                        LineString(
                            arrayOf(
                                pt(179.0, .0),
                                pt(180.0, .0)
                            )
                        )
                    ),
                    Feature(Point(pt(.0, .0)))
                )
            )
        ) shouldBeClose target(179.5, .0)
    }

    @Test
    fun geocentroid_of_a_non_empty_linestring_and_a_point_only_considers_the_line_string() {
        centroid(
            GeometryCollection(
                arrayOf(
                    LineString(
                        arrayOf(
                            pt(179.0, .0),
                            pt(180.0, .0)
                        )
                    ),
                    Point(pt(.0, .0))
                )
            )
        ) shouldBeClose target(179.5, .0)
    }

    @Test
    fun geocentroid_of_a_non_empty_polygon_a_non_empty_linestring_and_a_point_only_considers_the_polygon() {
        val lon = -179.5
        val lat = 0.500006
        centroid(
            GeometryCollection(
                arrayOf(
                    Polygon(
                        arrayOf(
                            arrayOf(
                                pt(-180.0, .0),
                                pt(-180.0, 1.0),
                                pt(-179.0, 1.0),
                                pt(-179.0, .0),
                                pt(-180.0, .0)
                            )
                        )
                    ),
                    LineString(
                        arrayOf(
                            pt(179.0, .0),
                            pt(180.0, .0)
                        )
                    ),
                    Point(pt(.0, .0))
                )
            )
        ) shouldBeClose target(lon, lat)

        centroid(
            GeometryCollection(
                arrayOf(
                    Point(pt(.0, .0)),
                    LineString(
                        arrayOf(
                            pt(179.0, .0),
                            pt(180.0, .0)
                        )
                    ),
                    Polygon(
                        arrayOf(
                            arrayOf(
                                pt(-180.0, .0),
                                pt(-180.0, 1.0),
                                pt(-179.0, 1.0),
                                pt(-179.0, .0),
                                pt(-180.0, .0)
                            )
                        )
                    )
                )
            )
        ) shouldBeClose target(-179.5, 0.500006)
    }

    @Test
    fun geocentroid_of_a_the_sphere_and_a_point_is_the_point() {
        centroid(
            FeatureCollection(
                arrayOf(
                    Feature(Sphere()),
                    Feature(Point(pt(1.0, 2.0)))
                )
            )
        ) shouldBeClose target(1.0, 2.0)

        centroid(
            FeatureCollection(
                arrayOf(
                    Feature(Point(pt(2.0, 3.0))),
                    Feature(Sphere())
                )
            )
        ) shouldBeClose target(2.0, 3.0)
    }

    private fun target(lon: Double, lat: Double) = GeoPoint(lon.deg, lat.deg)

    infix fun GeoPoint?.shouldBeClose(other: GeoPoint) {
        if (this == null)
            throw AssertionError("Result should not be null")

        lon.rad shouldBeClose other.lon.rad
        lat.rad shouldBeClose other.lat.rad
    }


    private fun centroid(geo: GeoJsonObject): GeoPoint? = GeoCentroidStream().centroid(geo)

    /*
tape("the drawCentroid of a detailed feature is correct", function(test) {
  var ny = require("./data/ny.json");
  test.inDelta(d3.geoCentroid(ny), [-73.93079, 40.69447], 1e-5);
  test.end();
});

tape("the drawCentroid of a set of polygons is the (spherical) average of its surface", function(test) {
  var circle = d3.geoCircle();
  test.inDelta(d3.geoCentroid({
    type: "MultiPolygon",
    coordinates: [
      circle.radius(45).center([90, 0])().coordinates,
      circle.radius(60).center([-90, 0])().coordinates
    ]
  }), [-90, 0], 1e-6);
  test.end();
});

tape("the drawCentroid of a small circle is its center: 5°", function(test) {
  test.inDelta(d3.geoCentroid(d3.geoCircle().radius(5).center([30, 45])()), [30, 45], 1e-6);
  test.end();
});

tape("the drawCentroid of a small circle is its center: 135°", function(test) {
  test.inDelta(d3.geoCentroid(d3.geoCircle().radius(135).center([30, 45])()), [30, 45], 1e-6);
  test.end();
});

tape("the drawCentroid of a small circle is its center: concentric rings", function(test) {
  var circle = d3.geoCircle().center([0, 45]),
      coordinates = circle.radius(60)().coordinates;
  coordinates.push(circle.radius(45)().coordinates[0].reverse());
  test.inDelta(d3.geoCentroid({type: "Polygon", coordinates: coordinates}), [0, 45], 1e-6);
  test.end();
});

tape("concentric rings", function(test) {
  var circle = d3.geoCircle().center([0, 45]),
      coordinates = circle.radius(60)().coordinates;
  coordinates.push(circle.radius(45)().coordinates[0].reverse());
  test.inDelta(d3.geoCentroid({type: "Polygon", coordinates: coordinates}), [0, 45], 1e-6);
  test.end();
});
     */
}