<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'

const props = defineProps({
  isTyping: {
    type: Boolean,
    default: false
  },
  showPassword: {
    type: Boolean,
    default: false
  },
  passwordLength: {
    type: Number,
    default: 0
  }
})

// Mouse tracking
const mouseX = ref(0)
const mouseY = ref(0)

// Blinking states
const isPurpleBlinking = ref(false)
const isBlackBlinking = ref(false)
const isLookingAtEachOther = ref(false)
const isPurplePeeking = ref(false)

// Refs for elements
const purpleRef = ref(null)
const blackRef = ref(null)
const yellowRef = ref(null)
const orangeRef = ref(null)

// Event handlers
const handleMouseMove = (e) => {
  mouseX.value = e.clientX
  mouseY.value = e.clientY
}

// Calculate position based on mouse
const calculatePosition = (ref) => {
  if (!ref.value) return { faceX: 0, faceY: 0, bodySkew: 0 }

  const rect = ref.value.getBoundingClientRect()
  const centerX = rect.left + rect.width / 2
  const centerY = rect.top + rect.height / 3

  const deltaX = mouseX.value - centerX
  const deltaY = mouseY.value - centerY

  const faceX = Math.max(-15, Math.min(15, deltaX / 20))
  const faceY = Math.max(-10, Math.min(10, deltaY / 30))
  const bodySkew = Math.max(-6, Math.min(6, -deltaX / 120))

  return { faceX, faceY, bodySkew }
}

const purplePos = computed(() => calculatePosition(purpleRef))
const blackPos = computed(() => calculatePosition(blackRef))
const yellowPos = computed(() => calculatePosition(yellowRef))
const orangePos = computed(() => calculatePosition(orangeRef))

const isHidingPassword = computed(() => props.passwordLength > 0 && !props.showPassword)

// Fixed "look away" rotation when hiding password - eyes only, body stays still
// (removed body skew, characters just look away with eyes)

// Blinking for purple character
let purpleBlinkTimer = null
const schedulePurpleBlink = () => {
  const interval = Math.random() * 4000 + 3000
  purpleBlinkTimer = setTimeout(() => {
    isPurpleBlinking.value = true
    setTimeout(() => {
      isPurpleBlinking.value = false
      schedulePurpleBlink()
    }, 150)
  }, interval)
}

// Blinking for black character
let blackBlinkTimer = null
const scheduleBlackBlink = () => {
  const interval = Math.random() * 4000 + 3000
  blackBlinkTimer = setTimeout(() => {
    isBlackBlinking.value = true
    setTimeout(() => {
      isBlackBlinking.value = false
      scheduleBlackBlink()
    }, 150)
  }, interval)
}

// Purple peeking animation
let purplePeekTimer = null
const schedulePurplePeek = () => {
  if (props.passwordLength > 0 && props.showPassword) {
    const interval = Math.random() * 3000 + 2000
    purplePeekTimer = setTimeout(() => {
      isPurplePeeking.value = true
      setTimeout(() => {
        isPurplePeeking.value = false
        schedulePurplePeek()
      }, 800)
    }, interval)
  }
}

onMounted(() => {
  window.addEventListener('mousemove', handleMouseMove)
  schedulePurpleBlink()
  scheduleBlackBlink()
  schedulePurplePeek()
})

onUnmounted(() => {
  window.removeEventListener('mousemove', handleMouseMove)
  if (purpleBlinkTimer) clearTimeout(purpleBlinkTimer)
  if (blackBlinkTimer) clearTimeout(blackBlinkTimer)
  if (purplePeekTimer) clearTimeout(purplePeekTimer)
})

// Watch for typing to trigger "looking at each other"
import { watch } from 'vue'
watch(() => props.isTyping, (val) => {
  if (val) {
    isLookingAtEachOther.value = true
    setTimeout(() => {
      isLookingAtEachOther.value = false
    }, 800)
  } else {
    isLookingAtEachOther.value = false
  }
})
</script>

<template>
  <div class="animated-characters" style="width: 550px; height: 400px; position: relative;">
    <!-- Purple tall rectangle character - Back layer -->
    <div
      ref="purpleRef"
      class="character purple"
      :style="{
        height: isTyping ? '440px' : '400px',
        transform: (passwordLength > 0 && showPassword)
          ? 'skewX(0deg)'
          : isTyping
            ? `skewX(${(purplePos.bodySkew || 0) - 12}deg) translateX(40px)`
            : `skewX(${purplePos.bodySkew || 0}deg)`,
        transition: 'all 0.7s ease-in-out'
      }"
    >
      <!-- Eyes -->
      <div
        class="eyes"
        :style="{
          left: isHidingPassword ? '60px' : (passwordLength > 0 && showPassword) ? '20px' : isLookingAtEachOther ? '55px' : `${45 + purplePos.faceX}px`,
          top: isHidingPassword ? '30px' : (passwordLength > 0 && showPassword) ? '35px' : isLookingAtEachOther ? '65px' : `${40 + purplePos.faceY}px`,
          transition: 'all 0.7s ease-in-out'
        }"
      >
        <div
          class="eye-ball"
          :class="{ blinking: isPurpleBlinking }"
          :style="{
            width: '18px',
            height: isPurpleBlinking ? '2px' : '18px',
            backgroundColor: 'white',
            transition: 'height 0.1s'
          }"
        >
          <div
            v-if="!isPurpleBlinking"
            class="pupil"
            :style="{
              width: '7px',
              height: '7px',
              backgroundColor: '#2D2D2D',
              transform: `translate(${isHidingPassword ? -5 : (passwordLength > 0 && showPassword) ? (isPurplePeeking ? 4 : -4) : isLookingAtEachOther ? 3 : 0}px, ${isHidingPassword ? 0 : (passwordLength > 0 && showPassword) ? (isPurplePeeking ? 5 : -4) : isLookingAtEachOther ? 4 : 0}px)`,
              transition: 'transform 0.1s ease-out'
            }"
          ></div>
        </div>
        <div
          class="eye-ball"
          :class="{ blinking: isPurpleBlinking }"
          :style="{
            width: '18px',
            height: isPurpleBlinking ? '2px' : '18px',
            backgroundColor: 'white',
            transition: 'height 0.1s'
          }"
        >
          <div
            v-if="!isPurpleBlinking"
            class="pupil"
            :style="{
              width: '7px',
              height: '7px',
              backgroundColor: '#2D2D2D',
              transform: `translate(${isHidingPassword ? -5 : (passwordLength > 0 && showPassword) ? (isPurplePeeking ? 4 : -4) : isLookingAtEachOther ? 3 : 0}px, ${isHidingPassword ? 0 : (passwordLength > 0 && showPassword) ? (isPurplePeeking ? 5 : -4) : isLookingAtEachOther ? 4 : 0}px)`,
              transition: 'transform 0.1s ease-out'
            }"
          ></div>
        </div>
      </div>
    </div>

    <!-- Black tall rectangle character - Middle layer -->
    <div
      ref="blackRef"
      class="character black"
      :style="{
        transform: (passwordLength > 0 && showPassword)
          ? 'skewX(0deg)'
          : isLookingAtEachOther
            ? `skewX(${(blackPos.bodySkew || 0) * 1.5 + 10}deg) translateX(20px)`
            : isTyping
              ? `skewX(${(blackPos.bodySkew || 0) * 1.5}deg)`
              : `skewX(${blackPos.bodySkew || 0}deg)`,
        transition: 'all 0.7s ease-in-out'
      }"
    >
      <!-- Eyes -->
      <div
        class="eyes"
        :style="{
          left: isHidingPassword ? '40px' : (passwordLength > 0 && showPassword) ? '10px' : isLookingAtEachOther ? '32px' : `${26 + blackPos.faceX}px`,
          top: isHidingPassword ? '25px' : (passwordLength > 0 && showPassword) ? '28px' : isLookingAtEachOther ? '12px' : `${32 + blackPos.faceY}px`,
          transition: 'all 0.7s ease-in-out'
        }"
      >
        <div
          class="eye-ball small"
          :class="{ blinking: isBlackBlinking }"
          :style="{
            width: '16px',
            height: isBlackBlinking ? '2px' : '16px',
            backgroundColor: 'white',
            transition: 'height 0.1s'
          }"
        >
          <div
            v-if="!isBlackBlinking"
            class="pupil"
            :style="{
              width: '6px',
              height: '6px',
              backgroundColor: '#2D2D2D',
              transform: `translate(${isHidingPassword ? -4 : (passwordLength > 0 && showPassword) ? -4 : isLookingAtEachOther ? 0 : 0}px, ${isHidingPassword ? 0 : (passwordLength > 0 && showPassword) ? -4 : isLookingAtEachOther ? -4 : 0}px)`,
              transition: 'transform 0.1s ease-out'
            }"
          ></div>
        </div>
        <div
          class="eye-ball small"
          :class="{ blinking: isBlackBlinking }"
          :style="{
            width: '16px',
            height: isBlackBlinking ? '2px' : '16px',
            backgroundColor: 'white',
            transition: 'height 0.1s'
          }"
        >
          <div
            v-if="!isBlackBlinking"
            class="pupil"
            :style="{
              width: '6px',
              height: '6px',
              backgroundColor: '#2D2D2D',
              transform: `translate(${isHidingPassword ? -4 : (passwordLength > 0 && showPassword) ? -4 : isLookingAtEachOther ? 0 : 0}px, ${isHidingPassword ? 0 : (passwordLength > 0 && showPassword) ? -4 : isLookingAtEachOther ? -4 : 0}px)`,
              transition: 'transform 0.1s ease-out'
            }"
          ></div>
        </div>
      </div>
    </div>

    <!-- Orange semi-circle character - Front left -->
    <div
      ref="orangeRef"
      class="character orange"
      :style="{
        transform: (passwordLength > 0 && showPassword) ? 'skewX(0deg)' : `skewX(${orangePos.bodySkew || 0}deg)`,
        transition: 'all 0.7s ease-in-out'
      }"
    >
      <!-- Pupils only -->
      <div
        class="pupils-only"
        :style="{
          left: isHidingPassword ? '70px' : (passwordLength > 0 && showPassword) ? '50px' : `${82 + (orangePos.faceX || 0)}px`,
          top: isHidingPassword ? '80px' : (passwordLength > 0 && showPassword) ? '85px' : `${90 + (orangePos.faceY || 0)}px`,
          transition: 'all 0.2s ease-out'
        }"
      >
        <div
          class="pupil"
          :style="{
            width: '12px',
            height: '12px',
            backgroundColor: '#2D2D2D',
            transform: `translate(${isHidingPassword ? -5 : (passwordLength > 0 && showPassword) ? -5 : 0}px, ${isHidingPassword ? 0 : (passwordLength > 0 && showPassword) ? -4 : 0}px)`,
            transition: 'transform 0.1s ease-out'
          }"
        ></div>
        <div
          class="pupil"
          :style="{
            width: '12px',
            height: '12px',
            backgroundColor: '#2D2D2D',
            transform: `translate(${isHidingPassword ? -5 : (passwordLength > 0 && showPassword) ? -5 : 0}px, ${isHidingPassword ? 0 : (passwordLength > 0 && showPassword) ? -4 : 0}px)`,
            transition: 'transform 0.1s ease-out'
          }"
        ></div>
      </div>
    </div>

    <!-- Yellow tall rectangle character - Front right -->
    <div
      ref="yellowRef"
      class="character yellow"
      :style="{
        transform: (passwordLength > 0 && showPassword) ? 'skewX(0deg)' : `skewX(${yellowPos.bodySkew || 0}deg)`,
        transition: 'all 0.7s ease-in-out'
      }"
    >
      <!-- Pupils only -->
      <div
        class="pupils-only"
        :style="{
          left: isHidingPassword ? '40px' : (passwordLength > 0 && showPassword) ? '20px' : `${52 + (yellowPos.faceX || 0)}px`,
          top: isHidingPassword ? '30px' : (passwordLength > 0 && showPassword) ? '35px' : `${40 + (yellowPos.faceY || 0)}px`,
          transition: 'all 0.2s ease-out'
        }"
      >
        <div
          class="pupil"
          :style="{
            width: '12px',
            height: '12px',
            backgroundColor: '#2D2D2D',
            transform: `translate(${isHidingPassword ? -5 : (passwordLength > 0 && showPassword) ? -5 : 0}px, ${isHidingPassword ? 0 : (passwordLength > 0 && showPassword) ? -4 : 0}px)`,
            transition: 'transform 0.1s ease-out'
          }"
        ></div>
        <div
          class="pupil"
          :style="{
            width: '12px',
            height: '12px',
            backgroundColor: '#2D2D2D',
            transform: `translate(${isHidingPassword ? -5 : (passwordLength > 0 && showPassword) ? -5 : 0}px, ${isHidingPassword ? 0 : (passwordLength > 0 && showPassword) ? -4 : 0}px)`,
            transition: 'transform 0.1s ease-out'
          }"
        ></div>
      </div>
      <!-- Mouth -->
      <div
        class="mouth"
        :style="{
          left: isHidingPassword ? '30px' : (passwordLength > 0 && showPassword) ? '10px' : `${40 + (yellowPos.faceX || 0)}px`,
          top: isHidingPassword ? '85px' : (passwordLength > 0 && showPassword) ? '88px' : `${88 + (yellowPos.faceY || 0)}px`,
          transition: 'all 0.2s ease-out'
        }"
      ></div>
    </div>
  </div>
</template>

<style scoped>
.animated-characters {
  position: relative;
}

.character {
  position: absolute;
  bottom: 0;
  border-radius: 10px 10px 0 0;
}

.character.purple {
  left: 70px;
  width: 180px;
  background-color: #6C3FF5;
  z-index: 1;
}

.character.black {
  left: 240px;
  width: 120px;
  height: 310px;
  background-color: #2D2D2D;
  z-index: 2;
}

.character.orange {
  left: 0;
  width: 240px;
  height: 200px;
  background-color: #FF9B6B;
  border-radius: 120px 120px 0 0;
  z-index: 3;
}

.character.yellow {
  left: 310px;
  width: 140px;
  height: 230px;
  background-color: #E8D754;
  border-radius: 70px 70px 0 0;
  z-index: 4;
}

.eyes {
  display: flex;
  gap: 8px;
  position: absolute;
}

.eye-ball {
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  overflow: hidden;
}

.eye-ball.small {
  width: 16px;
  height: 16px;
}

.pupil {
  border-radius: 50%;
}

.pupils-only {
  display: flex;
  gap: 8px;
  position: absolute;
}

.mouth {
  position: absolute;
  width: 20px;
  height: 4px;
  background-color: #2D2D2D;
  border-radius: 2px;
}
</style>
