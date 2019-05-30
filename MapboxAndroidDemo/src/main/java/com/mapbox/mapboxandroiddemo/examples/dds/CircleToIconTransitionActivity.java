package com.mapbox.mapboxandroiddemo.examples.dds;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.linear;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

/**
 * Create a smooth visual transition between circles and icons based on zooming in and out.
 * Great for data visualization.
 */
public class CircleToIconTransitionActivity extends AppCompatActivity implements OnMapReadyCallback,
  MapboxMap.OnCameraMoveListener {

  private static final float BASE_CIRCLE_INITIAL_RADIUS = 3.4f;
  private static final float RADIUS_WHEN_CIRCLES_MATCH_ICON_RADIUS = 6f;
  private static final float ZOOM_LEVEL_FOR_START_OF_BASE_CIRCLE_EXPANSION = 11;
  private static final float ZOOM_LEVEL_FOR_SWITCH_FROM_CIRCLE_TO_ICON = 12;
  private static final String SOURCE_ID = "SOURCE_ID";
  private static final String ICON_LAYER_ID = "ICON_LAYER_ID";
  private static final String BASE_CIRCLE_LAYER_ID = "BASE_CIRCLE_LAYER_ID";
  private static final String SHADOW_CIRCLE_LAYER_ID = "SHADOW_CIRCLE_LAYER_ID";
  private static final String ICON_IMAGE_ID = "ICON_ID";
  private static final String TAG = "CircleToIcon";

  private MapView mapView;
  private MapboxMap mapboxMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_circle_to_icon_transition);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(final MapboxMap mapboxMap) {
    mapboxMap.setStyle(new Style.Builder().fromUri(Style.LIGHT)

            // Add images to the map so that the SymbolLayers can reference the images.
            .withImage(ICON_IMAGE_ID, BitmapUtils.getBitmapFromDrawable(
                getResources().getDrawable(R.drawable.atm_symbol_icon)))

            // Add random data to the GeoJsonSource and then add the GeoJsonSource to the map
            .withSource(new GeoJsonSource(SOURCE_ID,
                FeatureCollection.fromFeatures(new Feature[]{
                    Feature.fromGeometry(Point.fromLngLat(
                        135.5101776123047,
                        34.67839374011646)),
                    Feature.fromGeometry(Point.fromLngLat(
                        135.5486297607422,
                        34.71170250154446)),
                    Feature.fromGeometry(Point.fromLngLat(
                        135.56854248046875,
                        34.67839374011646)),
                    Feature.fromGeometry(Point.fromLngLat(
                        135.51223754882812,
                        34.621342549943115)),
                    Feature.fromGeometry(Point.fromLngLat(
                        135.54588317871094,
                        34.65410941913741)),
                    Feature.fromGeometry(Point.fromLngLat(
                        135.59051513671875,
                        34.63490282449189))
                })
            ))
        , new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {
            CircleToIconTransitionActivity.this.mapboxMap = mapboxMap;
            mapboxMap.addOnCameraMoveListener(CircleToIconTransitionActivity.this);
            CircleLayer baseCircleLayer = new CircleLayer(BASE_CIRCLE_LAYER_ID, SOURCE_ID).withProperties(
                circleColor(Color.parseColor("#3BC802")),
                circleRadius(
                    interpolate(
                        linear(), zoom(),
                        stop(0, BASE_CIRCLE_INITIAL_RADIUS),
                        stop(ZOOM_LEVEL_FOR_START_OF_BASE_CIRCLE_EXPANSION, BASE_CIRCLE_INITIAL_RADIUS),
                        stop(ZOOM_LEVEL_FOR_SWITCH_FROM_CIRCLE_TO_ICON, RADIUS_WHEN_CIRCLES_MATCH_ICON_RADIUS)
                    )
                )
            );
            baseCircleLayer.setMaxZoom(ZOOM_LEVEL_FOR_SWITCH_FROM_CIRCLE_TO_ICON);
            style.addLayer(baseCircleLayer);

            CircleLayer shadowTransitionCircleLayer = new CircleLayer(SHADOW_CIRCLE_LAYER_ID, SOURCE_ID).withProperties(
              circleColor(Color.parseColor("#858585")),
              circleRadius(10f),
              circleOpacity(
                interpolate(
                  linear(), zoom(),
                  stop(ZOOM_LEVEL_FOR_START_OF_BASE_CIRCLE_EXPANSION - .1, 0),
                  stop(ZOOM_LEVEL_FOR_START_OF_BASE_CIRCLE_EXPANSION, .5f)
                )
              )
            );
            shadowTransitionCircleLayer.setMaxZoom(ZOOM_LEVEL_FOR_SWITCH_FROM_CIRCLE_TO_ICON);
            style.addLayerBelow(shadowTransitionCircleLayer, BASE_CIRCLE_LAYER_ID);


           SymbolLayer symbolIconLayer = new SymbolLayer(ICON_LAYER_ID, SOURCE_ID);
            symbolIconLayer.withProperties(
              iconImage(ICON_IMAGE_ID),
              iconIgnorePlacement(true),
              iconAllowOverlap(true)
              /*iconOpacity(
                interpolate(
                  linear(), zoom(),
                  stop(ZOOM_LEVEL_FOR_START_OF_BASE_CIRCLE_EXPANSION - .1, 0),
                  stop(ZOOM_LEVEL_FOR_SWITCH_FROM_CIRCLE_TO_ICON, .5f)
                )
              )*/
            );

            symbolIconLayer.setMinZoom(ZOOM_LEVEL_FOR_SWITCH_FROM_CIRCLE_TO_ICON);
            style.addLayer(symbolIconLayer);

            Toast.makeText(CircleToIconTransitionActivity.this,
                R.string.zoom_map_in_and_out_circle_to_icon_transition, Toast.LENGTH_SHORT).show();
          }
        }
    );

  }

  @Override
  public void onCameraMove() {
    Log.d(TAG, "zoom = " + mapboxMap.getCameraPosition().zoom);
  }

  @Override
  public void onStart() {
    super.onStart();
    mapView.onStart();
  }

  @Override
  public void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  public void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  public void onStop() {
    super.onStop();
    mapView.onStop();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mapboxMap != null) {
      mapboxMap.removeOnCameraMoveListener(this);
    }
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}
