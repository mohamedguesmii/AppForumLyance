import { Component, OnInit } from '@angular/core';

declare const google: any;

@Component({
  selector: 'app-maps',
  templateUrl: './calendar.component.html',
  styleUrls: ['./calendar.component.css']
})
export class CalendarComponent implements OnInit {

  constructor() { }

  ngOnInit() {
    this.initializeMap();
  }

  initializeMap() {
    const myLatlng = new google.maps.LatLng(40.748817, -73.985428);
    const mapOptions = {
      zoom: 13,
      center: myLatlng,
      scrollwheel: false, // Disable scroll over the map
      styles: [
        { "featureType": "water", "stylers": [{ "saturation": 43 }, { "lightness": -11 }, { "hue": "#0088ff" }] },
        { "featureType": "road", "elementType": "geometry.fill", "stylers": [{ "hue": "#ff0000" }, { "saturation": -100 }, { "lightness": 99 }] },
        { "featureType": "road", "elementType": "geometry.stroke", "stylers": [{ "color": "#808080" }, { "lightness": 54 }] },
        { "featureType": "landscape.man_made", "elementType": "geometry.fill", "stylers": [{ "color": "#ece2d9" }] },
        { "featureType": "poi.park", "elementType": "geometry.fill", "stylers": [{ "color": "#ccdca1" }] },
        { "featureType": "road", "elementType": "labels.text.fill", "stylers": [{ "color": "#767676" }] },
        { "featureType": "road", "elementType": "labels.text.stroke", "stylers": [{ "color": "#ffffff" }] },
        { "featureType": "poi", "stylers": [{ "visibility": "off" }] },
        { "featureType": "landscape.natural", "elementType": "geometry.fill", "stylers": [{ "visibility": "on" }, { "color": "#b8cb93" }] },
        { "featureType": "poi.park", "stylers": [{ "visibility": "on" }] },
        { "featureType": "poi.sports_complex", "stylers": [{ "visibility": "on" }] },
        { "featureType": "poi.medical", "stylers": [{ "visibility": "on" }] },
        { "featureType": "poi.business", "stylers": [{ "visibility": "simplified" }] }
      ]
    };

    const map = new google.maps.Map(document.getElementById("map"), mapOptions);

    const marker = new google.maps.Marker({
      position: myLatlng,
      title: "Hello World!"
    });

    marker.setMap(map);
  }

}
