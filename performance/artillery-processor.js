/**
 * Artillery Processor for Open-Spot Performance Tests
 *
 * This file contains custom functions for Artillery test scenarios.
 */

module.exports = {
  /**
   * Set Authorization header with a test token
   *
   * For actual load testing with authenticated endpoints, you should:
   * 1. Create a test user
   * 2. Generate a valid JWT token
   * 3. Set it as an environment variable: TEST_JWT_TOKEN
   */
  setAuthHeader: function(requestParams, context, ee, next) {
    const token = process.env.TEST_JWT_TOKEN || 'test-token-placeholder';
    requestParams.headers = requestParams.headers || {};
    requestParams.headers['Authorization'] = `Bearer ${token}`;
    return next();
  },

  /**
   * Generate random coordinates within Seoul area
   */
  generateSeoulCoordinates: function(context, events, done) {
    // Seoul approximate bounds
    const MIN_LAT = 37.4;
    const MAX_LAT = 37.7;
    const MIN_LON = 126.8;
    const MAX_LON = 127.2;

    context.vars.latitude = MIN_LAT + Math.random() * (MAX_LAT - MIN_LAT);
    context.vars.longitude = MIN_LON + Math.random() * (MAX_LON - MIN_LON);
    context.vars.radiusMeters = 1000 + Math.random() * 4000; // 1km ~ 5km

    return done();
  },

  /**
   * Log custom metrics
   */
  logCustomMetric: function(requestParams, response, context, ee, next) {
    if (response && response.timings) {
      const totalTime = response.timings.phases.total;
      ee.emit('customStat', {
        stat: 'response_time_custom',
        value: totalTime
      });
    }
    return next();
  }
};
