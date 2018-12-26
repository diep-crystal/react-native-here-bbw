/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from 'react';
import { AppRegistry } from 'react-native';

import HereMap from './HereMap';

export default class HereMapsRN extends Component {

  render() {
    return (

      <HereMap
        style={{flex : 1, backgroundColor: 'yellow'}}
        center="37.615223,-122.431297"
        mapType="normal"
        initialZoom={9} />
    );
  }
}

AppRegistry.registerComponent('HereMapsRN', () => HereMapsRN);
