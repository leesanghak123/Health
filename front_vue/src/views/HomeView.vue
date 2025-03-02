<template>
  <div class="home">
    <button @click="getData">버튼을 눌러봐</button>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'HomeView',
  methods: {
    getData() {
      axios
        .get('http://localhost:8002')
        .then((response) => {
          console.log(response)
        })
        .catch((error) => {
          console.log(error)
        })
    }
  }
}
</script>
