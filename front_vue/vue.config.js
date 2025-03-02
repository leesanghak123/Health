// @vue/cli-service: Vue CLI 프로젝트에서 기본적인 Webpack 설정을 제공하는 라이브러리
const { defineConfig } = require('@vue/cli-service')

// module.exports: 파일을 외부(다른 파일)에서 사용 가능하게 만드는 부분
module.exports = defineConfig({
  transpileDependencies: true, // 외부 라이브러리 코드도 최신 JS 문법으로 변환
  
  // 문제점1 해결
  configureWebpack: { // configureWebpack: Webpack 설정 커스터마이징
    resolve: {
      fallback: {
        "crypto": false  // crypto 모듈 사용하지 않도록 설정
      }
    },

    // 문제점2 해결
    plugins: [ // Webpack 플러그인을 설정 하는 부분
      // feature flag 추가
      new (require('webpack')).DefinePlugin({
        __VUE_PROD_HYDRATION_MISMATCH_DETAILS__: JSON.stringify(false)  // flag 비활성화 (오류 메시지 출력 X)
      })
    ]
  }
})