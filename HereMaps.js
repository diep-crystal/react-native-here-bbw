import PropTypes from 'prop-types';

import React from 'react';
import {
  View,
  Button,
  NativeModules,
  requireNativeComponent,
  findNodeHandle,
  NativeEventEmitter,
  ViewPropTypes

} from 'react-native';

const MAP_TYPES = {
  NORMAL: 'normal',
  SATELLITE: 'satellite',
};

const UIManager = NativeModules.UIManager;

const timer = () => { };
class HereMaps extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      isReady: false,
      zoomLevel: 15,
      center: this.props.center,
      onTouchEnd: false,
      lastLocation: null
    };

    this._onMapReady = this._onMapReady.bind(this);
  }

  componentDidMount() {
    const { isReady } = this.state;
    if (isReady) {
    }

    const EVENT_NAME = new NativeEventEmitter(UIManager);
    this.subscription = EVENT_NAME.addListener('HERE_MAP_ON_CHANGED',
      (location) => {
        this.countdownTimer(location)
      });
  }

  countdownTimer = (location) => {
    clearInterval(timer);
    timer = setInterval(() => {
      if (this.state.onTouchEnd && this.state.lastLocation != location) {
        this.props.onMapChanged(JSON.parse(location))
        this.setState({ lastLocation: location })
      }
      clearInterval(timer);
    }, 10);
  }

  render() {
    return (
      <View
        onTouchStart={() => this.setState({ onTouchEnd: false })}
        onTouchEnd={() => this.setState({ onTouchEnd: true })}
        style={this.props.style}>
        <HereMapView
          style={this.props.style}
          center={this.props.center}
          mapType={this.props.mapType}
          initialZoom={this.props.initialZoom} >

          <View style={{
            position: 'absolute', top: 10, right: 10,
            width: 50, height: 120,
            backgroundColor: 'yellow',
            justifyContent: 'space-between'
          }}>

            <Button
              title="+"
              onPress={this.onZoomInPress} />

            <Button
              title="-"
              onPress={this.onZoomOutPress} />

            <Button
              title="o"
              onPress={this.onSetCenterPress} />

          </View>
        </HereMapView>
      </View>
    );
  }

  _onMapReady() {
    this.setState({ isReady: true });
  }

  onZoomInPress = () => {
    if (this.state.zoomLevel < 20) {
      this.setState({ zoomLevel: this.state.zoomLevel + 1 });
      UIManager.dispatchViewManagerCommand(
        findNodeHandle(this),
        UIManager.HereMapView.Commands.zoomIn,
        [this.state.zoomLevel]);
    }
  }

  onZoomOutPress = () => {
    if (this.state.zoomLevel > 0) {
      this.setState({ zoomLevel: this.state.zoomLevel - 1 });
      UIManager.dispatchViewManagerCommand(
        findNodeHandle(this),
        UIManager.HereMapView.Commands.zoomOut,
        [this.state.zoomLevel]);
    }
  }

  onSetCenterPress = () => {
    this.setState({ center: this.state.center });
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this),
      UIManager.HereMapView.Commands.setCenter,
      [this.state.center]);
  }
}
HereMaps.propTypes = {
  ...ViewPropTypes, // include the default view properties
  style: ViewPropTypes.style,
  center: PropTypes.string,
  mapType: PropTypes.oneOf(Object.values(MAP_TYPES)),
  initialZoom: PropTypes.number
};

const HereMapView = requireNativeComponent('HereMapView', HereMaps);
module.exports = HereMaps;
