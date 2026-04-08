<script setup>
import {ref, computed, onMounted} from 'vue'
import {get, post} from '@/net'
import {ElMessage, ElMessageBox} from 'element-plus'

// ========== 内部分区切换 ==========
const activeTab = ref('reviews') // 'reviews' | 'discussions'

// ========== 课程评价相关 ==========
const reviews = ref([])
const likeStatusMap = ref({})
const repliesMap = ref({})
const replyContent = ref({})
const showReplyInput = ref({})
const showReplies = ref({})
const replyLikeStatusMap = ref({})
const replyToMap = ref({})

const courseFilter = ref('all')
const ratingFilter = ref('all')

const courseOptions = computed(() => {
  const map = new Map()
  reviews.value.forEach(r => { if (!map.has(r.courseId)) map.set(r.courseId, r.courseName) })
  discussions.value.forEach(d => { if (!map.has(d.courseId)) map.set(d.courseId, d.courseName) })
  return [...map.entries()].map(([id, name]) => ({label: name, value: id, name}))
})

const ratingOptions = [
  {label: '全部评分', value: 'all'},
  {label: '5星', value: '5'},
  {label: '4星及以上', value: '4'},
  {label: '3星及以上', value: '3'},
]

const filteredReviews = computed(() => {
  return reviews.value.filter(r => {
    if (courseFilter.value !== 'all' && r.courseId !== courseFilter.value) return false
    if (ratingFilter.value !== 'all' && r.rating < Number(ratingFilter.value)) return false
    return true
  })
})

const getInitials = (name) => name ? name.charAt(0) : '?'
const getRatingText = (v) => ({1: '很差', 2: '较差', 3: '一般', 4: '较好', 5: '很好'}[v] || '')
const formatDate = (d) => {
  if (!d) return ''
  const date = d.replace('T', ' ').split(' ')[0]
  return date.replace(/-/g, '/')
}
const avgRating = computed(() => {
  if (filteredReviews.value.length === 0) return '0.0'
  return (filteredReviews.value.reduce((s, r) => s + r.rating, 0) / filteredReviews.value.length).toFixed(1)
})

const loadReviews = () => {
  get('/api/course/community?type=review', (data) => {
    reviews.value = data || []
    loadLikeStatuses()
  })
}

const loadLikeStatuses = () => {
  if (reviews.value.length === 0) return
  const reviewIds = reviews.value.map(r => r.id)
  get(`/api/course/community/like-statuses?reviewIds=${reviewIds.join(',')}`, (likes) => {
    if (Array.isArray(likes)) {
      likes.forEach(like => {
        if (like && like.reviewId && like.type) {
          likeStatusMap.value[like.reviewId] = like.type
        }
      })
    }
  })
}

const toggleLike = (reviewId, type) => {
  post('/api/course/community/like', {reviewId, type}, (msg) => {
    const review = reviews.value.find(r => r.id === reviewId)
    if (!review) return

    const prevType = likeStatusMap.value[reviewId]
    if (prevType === type) {
      if (type === 1) review.likeCount = Math.max(0, (review.likeCount || 0) - 1)
      else review.dislikeCount = Math.max(0, (review.dislikeCount || 0) - 1)
      delete likeStatusMap.value[reviewId]
    } else {
      if (prevType === 1) review.likeCount = Math.max(0, (review.likeCount || 0) - 1)
      if (prevType === 2) review.dislikeCount = Math.max(0, (review.dislikeCount || 0) - 1)
      if (type === 1) review.likeCount = (review.likeCount || 0) + 1
      else review.dislikeCount = (review.dislikeCount || 0) + 1
      likeStatusMap.value[reviewId] = type
    }
  }, () => {
    ElMessage.error('操作失败')
  })
}

const loadReplies = (reviewId) => {
  get(`/api/course/community/replies?reviewId=${reviewId}`, (data) => {
    repliesMap.value[reviewId] = data || []
    showReplies.value[reviewId] = true
    loadRepliesLikeStatuses(data || [])
  })
}

const loadRepliesLikeStatuses = (replies) => {
  if (replies.length === 0) return
  const replyIds = replies.map(r => r.id)
  get(`/api/course/community/reply/like-statuses?replyIds=${replyIds.join(',')}`, (likes) => {
    if (Array.isArray(likes)) {
      likes.forEach(like => {
        if (like && like.replyId && like.type) {
          replyLikeStatusMap.value[like.replyId] = like.type
        }
      })
    }
  })
}

const submitReply = (reviewId) => {
  const content = (replyContent.value[reviewId] || '').trim()
  if (!content) {
    ElMessage.warning('请输入回复内容')
    return
  }
  const params = {reviewId, content}
  const replyTo = replyToMap.value[reviewId]
  if (replyTo && replyTo.replyId) {
    params.parentReplyId = replyTo.replyId
  }
  post('/api/course/community/reply', params, () => {
    ElMessage.success('回复成功')
    replyContent.value[reviewId] = ''
    showReplyInput.value[reviewId] = false
    delete replyToMap.value[reviewId]
    loadReplies(reviewId)
  }, () => {
    ElMessage.error('回复失败')
  })
}

const toggleReplyLike = (replyId, type) => {
  post('/api/course/community/reply/like', {replyId, type}, () => {
    Object.keys(repliesMap.value).forEach(reviewId => {
      const reply = repliesMap.value[reviewId].find(r => r.id === replyId)
      if (!reply) return

      const prevType = replyLikeStatusMap.value[replyId]
      if (prevType === type) {
        if (type === 1) reply.likeCount = Math.max(0, (reply.likeCount || 0) - 1)
        else reply.dislikeCount = Math.max(0, (reply.dislikeCount || 0) - 1)
        delete replyLikeStatusMap.value[replyId]
      } else {
        if (prevType === 1) reply.likeCount = Math.max(0, (reply.likeCount || 0) - 1)
        if (prevType === 2) reply.dislikeCount = Math.max(0, (reply.dislikeCount || 0) - 1)
        if (type === 1) reply.likeCount = (reply.likeCount || 0) + 1
        else reply.dislikeCount = (reply.dislikeCount || 0) + 1
        replyLikeStatusMap.value[replyId] = type
      }
    })
  }, () => {
    ElMessage.error('操作失败')
  })
}

const clickReplyTo = (reviewId, replyId, userId, username) => {
  replyToMap.value[reviewId] = {replyId, userId, username}
  showReplyInput.value[reviewId] = true
}

const deleteReply = (reviewId, replyId) => {
  ElMessageBox.confirm('确定要删除这条回复吗？', '删除确认', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    post('/api/course/community/reply/delete', {replyId}, () => {
      ElMessage.success('删除成功')
      repliesMap.value[reviewId] = repliesMap.value[reviewId].filter(r => r.id !== replyId)
    }, () => {
      ElMessage.error('删除失败')
    })
  }).catch(() => {})
}

// ========== 讨论相关 ==========
const discussions = ref([])
const discussionLikeStatusMap = ref({})
const discussionRepliesMap = ref({})
const discussionReplyContent = ref({})
const discussionShowReplyInput = ref({})
const discussionShowReplies = ref({})
const discussionReplyLikeStatusMap = ref({})
const discussionReplyToMap = ref({})

const showCreateDiscussion = ref(false)
const newDiscussionContent = ref('')
const selectedCourseId = ref(null)
const discussionAnonymous = ref(false)

const filteredDiscussions = computed(() => {
  return discussions.value.filter(d => {
    if (courseFilter.value !== 'all' && d.courseId !== courseFilter.value) return false
    return true
  })
})

const loadDiscussions = () => {
  get('/api/course/community?type=discussion', (data) => {
    discussions.value = data || []
    loadDiscussionLikeStatuses()
  })
}

const loadDiscussionLikeStatuses = () => {
  if (discussions.value.length === 0) return
  const ids = discussions.value.map(d => d.id)
  get(`/api/course/community/like-statuses?reviewIds=${ids.join(',')}`, (likes) => {
    if (Array.isArray(likes)) {
      likes.forEach(like => {
        if (like && like.reviewId && like.type) {
          discussionLikeStatusMap.value[like.reviewId] = like.type
        }
      })
    }
  })
}

const toggleDiscussionLike = (discussionId, type) => {
  post('/api/course/community/like', {reviewId: discussionId, type}, () => {
    const d = discussions.value.find(x => x.id === discussionId)
    if (!d) return

    const prevType = discussionLikeStatusMap.value[discussionId]
    if (prevType === type) {
      if (type === 1) d.likeCount = Math.max(0, (d.likeCount || 0) - 1)
      else d.dislikeCount = Math.max(0, (d.dislikeCount || 0) - 1)
      delete discussionLikeStatusMap.value[discussionId]
    } else {
      if (prevType === 1) d.likeCount = Math.max(0, (d.likeCount || 0) - 1)
      if (prevType === 2) d.dislikeCount = Math.max(0, (d.dislikeCount || 0) - 1)
      if (type === 1) d.likeCount = (d.likeCount || 0) + 1
      else d.dislikeCount = (d.dislikeCount || 0) + 1
      discussionLikeStatusMap.value[discussionId] = type
    }
  }, () => {
    ElMessage.error('操作失败')
  })
}

const loadDiscussionReplies = (discussionId) => {
  get(`/api/course/community/replies?reviewId=${discussionId}`, (data) => {
    discussionRepliesMap.value[discussionId] = data || []
    discussionShowReplies.value[discussionId] = true
    loadDiscussionRepliesLikeStatuses(data || [])
  })
}

const loadDiscussionRepliesLikeStatuses = (replies) => {
  if (replies.length === 0) return
  const replyIds = replies.map(r => r.id)
  get(`/api/course/community/reply/like-statuses?replyIds=${replyIds.join(',')}`, (likes) => {
    if (Array.isArray(likes)) {
      likes.forEach(like => {
        if (like && like.replyId && like.type) {
          discussionReplyLikeStatusMap.value[like.replyId] = like.type
        }
      })
    }
  })
}

const submitDiscussionReply = (discussionId) => {
  const content = (discussionReplyContent.value[discussionId] || '').trim()
  if (!content) {
    ElMessage.warning('请输入回复内容')
    return
  }
  const params = {reviewId: discussionId, content}
  const replyTo = discussionReplyToMap.value[discussionId]
  if (replyTo && replyTo.replyId) {
    params.parentReplyId = replyTo.replyId
  }
  post('/api/course/community/reply', params, () => {
    ElMessage.success('回复成功')
    discussionReplyContent.value[discussionId] = ''
    discussionShowReplyInput.value[discussionId] = false
    delete discussionReplyToMap.value[discussionId]
    loadDiscussionReplies(discussionId)
  }, () => {
    ElMessage.error('回复失败')
  })
}

const toggleDiscussionReplyLike = (replyId, type) => {
  post('/api/course/community/reply/like', {replyId, type}, () => {
    Object.keys(discussionRepliesMap.value).forEach(dId => {
      const reply = discussionRepliesMap.value[dId].find(r => r.id === replyId)
      if (!reply) return

      const prevType = discussionReplyLikeStatusMap.value[replyId]
      if (prevType === type) {
        if (type === 1) reply.likeCount = Math.max(0, (reply.likeCount || 0) - 1)
        else reply.dislikeCount = Math.max(0, (reply.dislikeCount || 0) - 1)
        delete discussionReplyLikeStatusMap.value[replyId]
      } else {
        if (prevType === 1) reply.likeCount = Math.max(0, (reply.likeCount || 0) - 1)
        if (prevType === 2) reply.dislikeCount = Math.max(0, (reply.dislikeCount || 0) - 1)
        if (type === 1) reply.likeCount = (reply.likeCount || 0) + 1
        else reply.dislikeCount = (reply.dislikeCount || 0) + 1
        discussionReplyLikeStatusMap.value[replyId] = type
      }
    })
  }, () => {
    ElMessage.error('操作失败')
  })
}

const clickDiscussionReplyTo = (discussionId, replyId, userId, username) => {
  discussionReplyToMap.value[discussionId] = {replyId, userId, username}
  discussionShowReplyInput.value[discussionId] = true
}

const deleteDiscussionReply = (discussionId, replyId) => {
  ElMessageBox.confirm('确定要删除这条回复吗？', '删除确认', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    post('/api/course/community/reply/delete', {replyId}, () => {
      ElMessage.success('删除成功')
      discussionRepliesMap.value[discussionId] = discussionRepliesMap.value[discussionId].filter(r => r.id !== replyId)
    }, () => {
      ElMessage.error('删除失败')
    })
  }).catch(() => {})
}

const createDiscussion = () => {
  if (!newDiscussionContent.value.trim()) {
    ElMessage.warning('请输入讨论内容')
    return
  }
  if (!selectedCourseId.value) {
    ElMessage.warning('请选择课程')
    return
  }
  post('/api/course/community/discussion', {
    courseId: selectedCourseId.value,
    content: newDiscussionContent.value.trim(),
    anonymous: discussionAnonymous.value
  }, () => {
    ElMessage.success('发布成功')
    showCreateDiscussion.value = false
    newDiscussionContent.value = ''
    selectedCourseId.value = null
    discussionAnonymous.value = false
    loadDiscussions()
  }, () => {
    ElMessage.error('发布失败')
  })
}

onMounted(() => {
  loadReviews()
  loadDiscussions()
})
</script>

<template>
  <div class="community">
    <!-- 分区切换 -->
    <div class="seg community-seg">
      <label class="seg-opt" :class="{ on: activeTab === 'reviews' }" @click="activeTab = 'reviews'">课程评价</label>
      <label class="seg-opt" :class="{ on: activeTab === 'discussions' }" @click="activeTab = 'discussions'">讨论</label>
    </div>

    <!-- ========== 课程评价区 ========== -->
    <div v-show="activeTab === 'reviews'">
      <div class="tab-header">
        <h2 class="header-title">学习社区</h2>
        <span class="review-count">{{ filteredReviews.length }} 条评价</span>
      </div>

      <div class="stats-bar">
        <div class="stat-item">
          <span class="stat-n">{{ filteredReviews.length }}</span>
          <span class="stat-l">条评价</span>
        </div>
        <template v-if="courseFilter !== 'all'">
          <div class="stat-sep"></div>
          <div class="stat-item">
            <span class="stat-n">{{ avgRating }}</span>
            <span class="stat-l">平均评分</span>
          </div>
        </template>
        <div class="stat-sep"></div>
        <div class="stat-item">
          <span class="stat-n">{{ courseOptions.length }}</span>
          <span class="stat-l">门课程</span>
        </div>
      </div>

      <div class="filter-bar">
        <div class="filter-select-wrap">
          <select v-model="courseFilter" class="filter-select">
            <option value="all">全部课程</option>
            <option v-for="c in courseOptions" :key="c.value" :value="c.value">{{ c.label }}</option>
          </select>
          <svg class="select-arrow" viewBox="0 0 12 12" fill="none" stroke="currentColor" width="12" height="12">
            <path d="M3 5l3 3 3-3" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="filter-select-wrap">
          <select v-model="ratingFilter" class="filter-select">
            <option v-for="r in ratingOptions" :key="r.value" :value="r.value">{{ r.label }}</option>
          </select>
          <svg class="select-arrow" viewBox="0 0 12 12" fill="none" stroke="currentColor" width="12" height="12">
            <path d="M3 5l3 3 3-3" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
      </div>

      <div class="review-list">
        <div v-for="review in filteredReviews" :key="review.id" class="review-card">
          <div class="review-left">
            <div class="avatar" :class="{ 'avatar-anonymous': review.isAnonymous }">
              <template v-if="review.isAnonymous">
                <svg viewBox="0 0 20 20" fill="currentColor" width="18" height="18">
                  <path d="M10 2a5 5 0 100 10 5 5 0 000-10zm0 11c-5.33 0-8 2.67-8 4v1h16v-1c0-1.33-2.67-4-8-4z" opacity="0.5"/>
                </svg>
              </template>
              <template v-else>{{ getInitials(review.username) }}</template>
            </div>
          </div>
          <div class="review-body">
            <div class="review-header">
              <span class="review-user">{{ review.isAnonymous ? '匿名用户' : review.username }}</span>
              <span class="review-date">{{ formatDate(review.createdAt) }}</span>
            </div>
            <div class="review-meta">
              <span class="course-tag">{{ review.courseName }}</span>
              <div class="stars">
                <svg v-for="i in 5" :key="i" viewBox="0 0 16 16" width="14" height="14"
                     :fill="i <= review.rating ? '#ff9500' : 'none'"
                     :stroke="i <= review.rating ? '#ff9500' : '#d2d2d7'" stroke-width="1.2">
                  <path d="M8 1.5l1.85 4.17L14 6.34l-3 2.92.71 4.13L8 11.4l-3.71 2 .71-4.13-3-2.92 4.15-.67z"/>
                </svg>
                <span class="rating-text">{{ getRatingText(review.rating) }}</span>
              </div>
            </div>
            <p class="review-content">{{ review.review }}</p>

            <div class="review-actions">
              <button class="act-btn" :class="{ active: likeStatusMap[review.id] === 1 }"
                      @click="toggleLike(review.id, 1)">
                <svg viewBox="0 0 20 20" width="15" height="15" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M6.5 3C4 3 2 5 2 7.5c0 5 8 10 8 10s8-5 8-10C18 5 16 3 13.5 3c-1.5 0-2.8.8-3.5 2-.7-1.2-2-2-3.5-2z"/>
                </svg>
                <span>{{ review.likeCount || 0 }}</span>
              </button>
              <button class="act-btn dislike" :class="{ active: likeStatusMap[review.id] === 2 }"
                      @click="toggleLike(review.id, 2)">
                <svg viewBox="0 0 20 20" width="15" height="15" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M6.5 3C4 3 2 5 2 7.5c0 5 8 10 8 10s8-5 8-10C18 5 16 3 13.5 3c-1.5 0-2.8.8-3.5 2-.7-1.2-2-2-3.5-2z"/>
                  <line x1="10" y1="13" x2="10" y2="17"/>
                </svg>
                <span>{{ review.dislikeCount || 0 }}</span>
              </button>
              <button class="act-btn" @click="showReplyInput[review.id] = !showReplyInput[review.id]">
                <svg viewBox="0 0 20 20" width="15" height="15" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M18 2L9 11"/>
                  <path d="M18 2l-4 16-5-5-5-1z"/>
                </svg>
                <span>回复</span>
              </button>
              <button v-if="!showReplies[review.id]"
                      class="act-btn"
                      @click="loadReplies(review.id)">
                <svg viewBox="0 0 20 20" width="15" height="15" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round">
                  <path d="M4 7l6 6 6-6"/>
                </svg>
                <span>查看回复</span>
              </button>
              <button v-else class="act-btn" @click="showReplies[review.id] = false">
                <svg viewBox="0 0 20 20" width="15" height="15" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round">
                  <path d="M4 13l6-6 6 6"/>
                </svg>
                <span>收起回复</span>
              </button>
            </div>

            <div v-if="showReplyInput[review.id]" class="reply-input-area">
              <div v-if="replyToMap[review.id]" class="replying-to">
                <span>回复 @{{ replyToMap[review.id].username }}</span>
                <button class="cancel-reply-btn" @click="delete replyToMap[review.id]">×</button>
              </div>
              <textarea v-model="replyContent[review.id]" :placeholder="replyToMap[review.id] ? `回复 @${replyToMap[review.id].username}…` : '写下你的回复…'" rows="2"
                        @keydown.enter.ctrl="submitReply(review.id)"></textarea>
              <div class="reply-input-foot">
                <span class="reply-hint">Ctrl + Enter 发送</span>
                <button class="reply-send-btn" @click="submitReply(review.id)">发送</button>
              </div>
            </div>

            <div v-if="showReplies[review.id] && repliesMap[review.id]" class="replies-section">
              <div v-for="reply in repliesMap[review.id]" :key="reply.id" class="reply-item">
                <div class="reply-avatar">
                  <template v-if="reply.isAnonymous">
                    <svg viewBox="0 0 20 20" fill="currentColor" width="14" height="14" style="color:#aeaeb2">
                      <path d="M10 2a5 5 0 100 10 5 5 0 000-10zm0 11c-5.33 0-8 2.67-8 4v1h16v-1c0-1.33-2.67-4-8-4z" opacity="0.5"/>
                    </svg>
                  </template>
                  <template v-else>{{ getInitials(reply.username) }}</template>
                </div>
                <div class="reply-body">
                  <span class="reply-user">{{ reply.isAnonymous ? '匿名用户' : reply.username }}</span>
                  <span v-if="reply.isAuthorReply" class="author-badge">作者</span>
                  <span v-if="reply.replyToUsername" class="reply-to-text">回复 @{{ reply.replyToUsername }}</span>
                  <span class="reply-text">{{ reply.content }}</span>
                  <span class="reply-time">{{ formatDate(reply.createdAt) }}</span>
                  <div class="reply-actions">
                    <button class="reply-act-btn" :class="{ active: replyLikeStatusMap[reply.id] === 1 }"
                            @click="toggleReplyLike(reply.id, 1)">
                      <svg viewBox="0 0 20 20" width="12" height="12" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M6.5 3C4 3 2 5 2 7.5c0 5 8 10 8 10s8-5 8-10C18 5 16 3 13.5 3c-1.5 0-2.8.8-3.5 2-.7-1.2-2-2-3.5-2z"/>
                      </svg>
                      <span>{{ reply.likeCount || 0 }}</span>
                    </button>
                    <button class="reply-act-btn reply-dislike" :class="{ active: replyLikeStatusMap[reply.id] === 2 }"
                            @click="toggleReplyLike(reply.id, 2)">
                      <svg viewBox="0 0 20 20" width="12" height="12" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M6.5 3C4 3 2 5 2 7.5c0 5 8 10 8 10s8-5 8-10C18 5 16 3 13.5 3c-1.5 0-2.8.8-3.5 2-.7-1.2-2-2-3.5-2z"/>
                        <line x1="10" y1="13" x2="10" y2="17"/>
                      </svg>
                      <span>{{ reply.dislikeCount || 0 }}</span>
                    </button>
                    <button class="reply-act-btn" @click="clickReplyTo(review.id, reply.id, reply.userId, reply.username)">
                      <svg viewBox="0 0 20 20" width="12" height="12" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M18 2L9 11"/>
                        <path d="M18 2l-4 16-5-5-5-1z"/>
                      </svg>
                      <span>回复</span>
                    </button>
                    <button v-if="reply.canDelete" class="reply-act-btn reply-delete" @click="deleteReply(review.id, reply.id)">
                      <svg viewBox="0 0 20 20" width="12" height="12" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M3 6h18M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/>
                      </svg>
                      <span>删除</span>
                    </button>
                  </div>
                </div>
              </div>
              <div v-if="repliesMap[review.id].length === 0" class="no-replies">暂无回复</div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="filteredReviews.length === 0" class="empty-state">
        <div class="empty-icon-wrap">
          <svg viewBox="0 0 48 48" fill="none" stroke="currentColor" width="36" height="36">
            <path d="M36 16c0-6.6-5.4-12-12-12S12 9.4 12 16c0 2.2.6 4.3 1.6 6L24 40l10.4-18c1-1.7 1.6-3.8 1.6-6z"
                  stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="24" cy="16" r="4" stroke-width="2"/>
          </svg>
        </div>
        <p class="empty-title">暂无评价</p>
        <p class="empty-desc">当前筛选条件下没有评价</p>
      </div>
    </div>

    <!-- ========== 讨论区 ========== -->
    <div v-show="activeTab === 'discussions'">
      <div class="tab-header">
        <h2 class="header-title">课程讨论</h2>
        <span class="review-count">{{ filteredDiscussions.length }} 条讨论</span>
      </div>

      <div class="stats-bar">
        <div class="stat-item">
          <span class="stat-n">{{ filteredDiscussions.length }}</span>
          <span class="stat-l">条讨论</span>
        </div>
        <div class="stat-sep"></div>
        <div class="stat-item">
          <span class="stat-n">{{ courseOptions.length }}</span>
          <span class="stat-l">门课程</span>
        </div>
      </div>

      <div class="filter-bar">
        <div class="filter-select-wrap">
          <select v-model="courseFilter" class="filter-select">
            <option value="all">全部课程</option>
            <option v-for="c in courseOptions" :key="c.value" :value="c.value">{{ c.label }}</option>
          </select>
          <svg class="select-arrow" viewBox="0 0 12 12" fill="none" stroke="currentColor" width="12" height="12">
            <path d="M3 5l3 3 3-3" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <button class="create-discussion-btn" @click="showCreateDiscussion = true">
          <svg viewBox="0 0 20 20" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <line x1="10" y1="4" x2="10" y2="16"/><line x1="4" y1="10" x2="16" y2="10"/>
          </svg>
          发起讨论
        </button>
      </div>

      <div class="review-list">
        <div v-for="d in filteredDiscussions" :key="d.id" class="review-card">
          <div class="review-left">
            <div class="avatar" :class="{ 'avatar-anonymous': d.isAnonymous }">
              <template v-if="d.isAnonymous">
                <svg viewBox="0 0 20 20" fill="currentColor" width="18" height="18">
                  <path d="M10 2a5 5 0 100 10 5 5 0 000-10zm0 11c-5.33 0-8 2.67-8 4v1h16v-1c0-1.33-2.67-4-8-4z" opacity="0.5"/>
                </svg>
              </template>
              <template v-else>{{ getInitials(d.username) }}</template>
            </div>
          </div>
          <div class="review-body">
            <div class="review-header">
              <span class="review-user">{{ d.isAnonymous ? '匿名用户' : d.username }}</span>
              <span class="review-date">{{ formatDate(d.createdAt) }}</span>
            </div>
            <div class="review-meta">
              <span class="course-tag">{{ d.courseName }}</span>
            </div>
            <p class="review-content">{{ d.review }}</p>

            <div class="review-actions">
              <button class="act-btn" :class="{ active: discussionLikeStatusMap[d.id] === 1 }"
                      @click="toggleDiscussionLike(d.id, 1)">
                <svg viewBox="0 0 20 20" width="15" height="15" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M6.5 3C4 3 2 5 2 7.5c0 5 8 10 8 10s8-5 8-10C18 5 16 3 13.5 3c-1.5 0-2.8.8-3.5 2-.7-1.2-2-2-3.5-2z"/>
                </svg>
                <span>{{ d.likeCount || 0 }}</span>
              </button>
              <button class="act-btn dislike" :class="{ active: discussionLikeStatusMap[d.id] === 2 }"
                      @click="toggleDiscussionLike(d.id, 2)">
                <svg viewBox="0 0 20 20" width="15" height="15" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M6.5 3C4 3 2 5 2 7.5c0 5 8 10 8 10s8-5 8-10C18 5 16 3 13.5 3c-1.5 0-2.8.8-3.5 2-.7-1.2-2-2-3.5-2z"/>
                  <line x1="10" y1="13" x2="10" y2="17"/>
                </svg>
                <span>{{ d.dislikeCount || 0 }}</span>
              </button>
              <button class="act-btn" @click="discussionShowReplyInput[d.id] = !discussionShowReplyInput[d.id]">
                <svg viewBox="0 0 20 20" width="15" height="15" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M18 2L9 11"/>
                  <path d="M18 2l-4 16-5-5-5-1z"/>
                </svg>
                <span>回复</span>
              </button>
              <button v-if="!discussionShowReplies[d.id]"
                      class="act-btn"
                      @click="loadDiscussionReplies(d.id)">
                <svg viewBox="0 0 20 20" width="15" height="15" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round">
                  <path d="M4 7l6 6 6-6"/>
                </svg>
                <span>查看回复</span>
              </button>
              <button v-else class="act-btn" @click="discussionShowReplies[d.id] = false">
                <svg viewBox="0 0 20 20" width="15" height="15" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round">
                  <path d="M4 13l6-6 6 6"/>
                </svg>
                <span>收起回复</span>
              </button>
            </div>

            <div v-if="discussionShowReplyInput[d.id]" class="reply-input-area">
              <div v-if="discussionReplyToMap[d.id]" class="replying-to">
                <span>回复 @{{ discussionReplyToMap[d.id].username }}</span>
                <button class="cancel-reply-btn" @click="delete discussionReplyToMap[d.id]">×</button>
              </div>
              <textarea v-model="discussionReplyContent[d.id]" :placeholder="discussionReplyToMap[d.id] ? `回复 @${discussionReplyToMap[d.id].username}…` : '写下你的回复…'" rows="2"
                        @keydown.enter.ctrl="submitDiscussionReply(d.id)"></textarea>
              <div class="reply-input-foot">
                <span class="reply-hint">Ctrl + Enter 发送</span>
                <button class="reply-send-btn" @click="submitDiscussionReply(d.id)">发送</button>
              </div>
            </div>

            <div v-if="discussionShowReplies[d.id] && discussionRepliesMap[d.id]" class="replies-section">
              <div v-for="reply in discussionRepliesMap[d.id]" :key="reply.id" class="reply-item">
                <div class="reply-avatar">
                  <template v-if="reply.isAnonymous">
                    <svg viewBox="0 0 20 20" fill="currentColor" width="14" height="14" style="color:#aeaeb2">
                      <path d="M10 2a5 5 0 100 10 5 5 0 000-10zm0 11c-5.33 0-8 2.67-8 4v1h16v-1c0-1.33-2.67-4-8-4z" opacity="0.5"/>
                    </svg>
                  </template>
                  <template v-else>{{ getInitials(reply.username) }}</template>
                </div>
                <div class="reply-body">
                  <span class="reply-user">{{ reply.isAnonymous ? '匿名用户' : reply.username }}</span>
                  <span v-if="reply.isAuthorReply" class="author-badge">作者</span>
                  <span v-if="reply.replyToUsername" class="reply-to-text">回复 @{{ reply.replyToUsername }}</span>
                  <span class="reply-text">{{ reply.content }}</span>
                  <span class="reply-time">{{ formatDate(reply.createdAt) }}</span>
                  <div class="reply-actions">
                    <button class="reply-act-btn" :class="{ active: discussionReplyLikeStatusMap[reply.id] === 1 }"
                            @click="toggleDiscussionReplyLike(reply.id, 1)">
                      <svg viewBox="0 0 20 20" width="12" height="12" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M6.5 3C4 3 2 5 2 7.5c0 5 8 10 8 10s8-5 8-10C18 5 16 3 13.5 3c-1.5 0-2.8.8-3.5 2-.7-1.2-2-2-3.5-2z"/>
                      </svg>
                      <span>{{ reply.likeCount || 0 }}</span>
                    </button>
                    <button class="reply-act-btn reply-dislike" :class="{ active: discussionReplyLikeStatusMap[reply.id] === 2 }"
                            @click="toggleDiscussionReplyLike(reply.id, 2)">
                      <svg viewBox="0 0 20 20" width="12" height="12" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M6.5 3C4 3 2 5 2 7.5c0 5 8 10 8 10s8-5 8-10C18 5 16 3 13.5 3c-1.5 0-2.8.8-3.5 2-.7-1.2-2-2-3.5-2z"/>
                        <line x1="10" y1="13" x2="10" y2="17"/>
                      </svg>
                      <span>{{ reply.dislikeCount || 0 }}</span>
                    </button>
                    <button class="reply-act-btn" @click="clickDiscussionReplyTo(d.id, reply.id, reply.userId, reply.username)">
                      <svg viewBox="0 0 20 20" width="12" height="12" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M18 2L9 11"/>
                        <path d="M18 2l-4 16-5-5-5-1z"/>
                      </svg>
                      <span>回复</span>
                    </button>
                    <button v-if="reply.canDelete" class="reply-act-btn reply-delete" @click="deleteDiscussionReply(d.id, reply.id)">
                      <svg viewBox="0 0 20 20" width="12" height="12" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M3 6h18M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/>
                      </svg>
                      <span>删除</span>
                    </button>
                  </div>
                </div>
              </div>
              <div v-if="discussionRepliesMap[d.id].length === 0" class="no-replies">暂无回复</div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="filteredDiscussions.length === 0" class="empty-state">
        <div class="empty-icon-wrap">
          <svg viewBox="0 0 48 48" fill="none" stroke="currentColor" width="36" height="36">
            <path d="M24 4C12.95 4 4 12.95 4 24s8.95 20 20 20 20-8.95 20-20S35.05 4 24 4z" stroke-width="2"/>
            <path d="M16 20h16M16 26h10" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <p class="empty-title">暂无讨论</p>
        <p class="empty-desc">发起第一个讨论话题吧</p>
      </div>
    </div>

    <!-- 发起讨论弹窗 -->
    <div v-if="showCreateDiscussion" class="dialog-overlay" @click.self="showCreateDiscussion = false">
      <div class="dialog-card">
        <div class="dialog-header">
          <span class="dialog-title">发起讨论</span>
          <button class="dialog-close" @click="showCreateDiscussion = false">×</button>
        </div>
        <div class="dialog-body">
          <div class="dialog-section">
            <label class="sec-title">选择课程</label>
            <select v-model="selectedCourseId" class="filter-select" style="width:100%">
              <option :value="null" disabled>请选择课程</option>
              <option v-for="c in courseOptions" :key="c.value" :value="c.value">{{ c.label }}</option>
            </select>
          </div>
          <div class="dialog-section">
            <label class="sec-title">讨论内容</label>
            <textarea v-model="newDiscussionContent" class="discussion-textarea"
                      placeholder="分享你的想法或提出问题…" rows="5" maxlength="1000"></textarea>
          </div>
          <div class="dialog-section">
            <label class="anonymous-toggle" @click="discussionAnonymous = !discussionAnonymous">
              <span class="toggle-box" :class="{ active: discussionAnonymous }">
                <svg v-if="discussionAnonymous" viewBox="0 0 16 16" width="10" height="10" fill="#fff">
                  <path d="M13.78 4.22a.75.75 0 010 1.06l-7.25 7.25a.75.75 0 01-1.06 0L2.22 9.28a.75.75 0 011.06-1.06L6 10.94l6.72-6.72a.75.75 0 011.06 0z"/>
                </svg>
              </span>
              <span class="toggle-label">匿名发布</span>
            </label>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="dlg-btn dlg-cancel" @click="showCreateDiscussion = false">取消</button>
          <button class="dlg-btn dlg-confirm" @click="createDiscussion">发布</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

.community {
  width: 100%;
  padding: 8px;
  font-family: -apple-system, 'SF Pro Text', 'PingFang SC', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
}

/* 分区切换 */
.community-seg {
  display: flex;
  gap: 4px;
  margin-bottom: 20px;
  padding: 4px;
  background: rgba(0,0,0,0.04);
  border-radius: 12px;
}
.seg-opt {
  flex: 1;
  text-align: center;
  padding: 9px 16px;
  border-radius: 9px;
  font-size: 14px;
  font-weight: 500;
  color: #86868b;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
}
.seg-opt:hover { color: #1d1d1f; }
.seg-opt.on {
  background: rgba(255,255,255,0.85);
  color: #1d1d1f;
  font-weight: 600;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}

.tab-header { display: flex; align-items: baseline; gap: 14px; margin-bottom: 20px; }
.header-title { font-size: 26px; font-weight: 700; letter-spacing: -0.03em; color: #1d1d1f; }
.review-count { font-size: 14px; color: #86868b; }

/* Stats bar */
.stats-bar {
  display: flex; align-items: center;
  border-radius: 12px; padding: 14px 0;
  background: rgba(0,0,0,0.04);
  box-shadow: 0 2px 4px rgba(0,0,0,0.07) inset, 0 1px 0 rgba(255,255,255,0.88);
  border: 0.5px solid rgba(0,0,0,0.06);
  margin-bottom: 20px;
}
.stat-item { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 3px; }
.stat-n { font-size: 22px; font-weight: 700; letter-spacing: -0.04em; color: #1d1d1f; line-height: 1; }
.stat-l { font-size: 11px; color: #86868b; }
.stat-sep { width: 0.5px; height: 30px; background: rgba(0,0,0,0.08); }

/* Filter bar */
.filter-bar { display: flex; gap: 10px; margin-bottom: 20px; flex-wrap: wrap; }
.filter-select-wrap { position: relative; flex: 1; min-width: 140px; }
.filter-select {
  width: 100%; height: 40px;
  padding: 0 32px 0 14px;
  border-radius: 10px;
  border: 0.5px solid rgba(0,0,0,0.1);
  background: rgba(255,255,255,0.6);
  backdrop-filter: blur(12px);
  font-size: 13px; font-weight: 500;
  font-family: inherit; color: #1d1d1f;
  appearance: none; -webkit-appearance: none;
  cursor: pointer; outline: none;
  transition: all 0.15s ease;
}
.filter-select:focus {
  border-color: rgba(0,113,227,0.4);
  box-shadow: 0 0 0 3px rgba(0,113,227,0.12);
}
.select-arrow {
  position: absolute; right: 12px; top: 50%; transform: translateY(-50%);
  color: #86868b; pointer-events: none;
}

/* 发起讨论按钮 */
.create-discussion-btn {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 9px 18px; border-radius: 10px;
  border: none; background: rgba(29,29,31,0.88);
  color: #fff; font-size: 13px; font-weight: 600;
  font-family: inherit; cursor: pointer;
  transition: all 0.15s ease; white-space: nowrap;
}
.create-discussion-btn:hover {
  background: rgba(0,0,0,0.95);
  transform: translateY(-1px);
}

/* Review list */
.review-list { display: flex; flex-direction: column; gap: 12px; }
.review-card {
  display: flex; gap: 14px;
  border-radius: 16px; padding: 18px;
  background: rgba(255,255,255,0.52);
  backdrop-filter: saturate(200%) blur(40px);
  border: 0.5px solid rgba(255,255,255,0.85);
  box-shadow: 0 2px 32px rgba(0,0,0,0.10), 0 0.5px 0 rgba(255,255,255,0.95) inset;
  transition: transform 0.22s cubic-bezier(0.34,1.56,0.64,1), box-shadow 0.22s ease;
}
.review-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 24px rgba(0,0,0,0.12), 0 0.5px 0 rgba(255,255,255,0.95) inset;
}

.review-left { flex-shrink: 0; }
.avatar {
  width: 42px; height: 42px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1d1d1f 0%, #3a3a3c 100%);
  color: #fff; font-size: 16px; font-weight: 600;
  display: flex; align-items: center; justify-content: center;
  letter-spacing: -0.02em;
}
.avatar-anonymous {
  background: linear-gradient(135deg, #8e8e93 0%, #aeaeb2 100%);
}

.review-body { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 8px; }
.review-header { display: flex; justify-content: space-between; align-items: center; }
.review-user { font-size: 15px; font-weight: 600; color: #1d1d1f; letter-spacing: -0.01em; }
.review-date { font-size: 12px; color: #aeaeb2; }
.review-meta { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.course-tag {
  display: inline-flex; align-items: center;
  padding: 2px 10px; border-radius: 20px;
  background: rgba(0,0,0,0.06);
  font-size: 12px; font-weight: 500; color: #6e6e73;
  white-space: nowrap;
}
.stars { display: inline-flex; align-items: center; gap: 2px; }
.rating-text { font-size: 11px; color: #aeaeb2; margin-left: 4px; font-weight: 500; }
.review-content { font-size: 14px; color: #1d1d1f; line-height: 1.65; letter-spacing: -0.01em; }

/* Actions */
.review-actions { display: flex; gap: 6px; flex-wrap: wrap; }

.act-btn {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 5px 12px; border-radius: 18px;
  border: none; background: rgba(0,0,0,0.04);
  color: #86868b; font-size: 12px; font-weight: 500;
  font-family: inherit; cursor: pointer;
  transition: all 0.18s ease;
}
.act-btn:hover { background: rgba(0,0,0,0.08); color: #1d1d1f; }
.act-btn.active { background: rgba(0,113,227,0.1); color: #0071e3; }
.act-btn.dislike.active { background: rgba(255,59,48,0.08); color: #ff3b30; }

/* Reply input */
.reply-input-area {
  background: rgba(0,0,0,0.02);
  border-radius: 12px; padding: 12px;
  border: 0.5px solid rgba(0,0,0,0.06);
}
.replying-to {
  display: flex; align-items: center; justify-content: space-between;
  padding: 4px 8px; margin-bottom: 8px;
  background: rgba(0,113,227,0.08);
  border-radius: 8px; font-size: 12px; color: #0071e3; font-weight: 500;
}
.cancel-reply-btn {
  width: 18px; height: 18px;
  border: none; background: rgba(0,0,0,0.06);
  border-radius: 50%; color: #86868b;
  font-size: 16px; line-height: 1;
  cursor: pointer; transition: all 0.15s;
}
.cancel-reply-btn:hover { background: rgba(0,0,0,0.1); color: #1d1d1f; }

.reply-input-area textarea {
  width: 100%; border: none; outline: none;
  background: rgba(255,255,255,0.6);
  border-radius: 10px; padding: 10px 12px;
  font-size: 13px; font-family: inherit;
  line-height: 1.5; resize: none; color: #1d1d1f;
}
.reply-input-area textarea::placeholder { color: #aeaeb2; }
.reply-input-area textarea:focus {
  background: #fff;
  box-shadow: 0 0 0 2px rgba(0,113,227,0.12);
}
.reply-input-foot { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }
.reply-hint { font-size: 11px; color: #aeaeb2; }
.reply-send-btn {
  padding: 5px 16px; border-radius: 14px;
  border: none; background: #0071e3;
  color: #fff; font-size: 12px; font-weight: 600;
  font-family: inherit; cursor: pointer; transition: all 0.15s;
}
.reply-send-btn:hover { background: #0077ed; }

/* Replies section */
.replies-section {
  background: rgba(0,0,0,0.02);
  border-radius: 12px; padding: 12px;
  display: flex; flex-direction: column; gap: 10px;
}
.reply-item { display: flex; gap: 8px; }
.reply-avatar {
  width: 28px; height: 28px; border-radius: 50%;
  background: linear-gradient(135deg, #6e6e73, #8e8e93);
  color: #fff; font-size: 11px; font-weight: 600;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.reply-body {
  display: flex; flex-wrap: wrap; align-items: baseline; gap: 4px 8px;
  min-width: 0;
}
.reply-user { font-size: 13px; font-weight: 600; color: #1d1d1f; }
.author-badge {
  display: inline-flex; align-items: center;
  padding: 1px 6px; border-radius: 10px;
  background: linear-gradient(135deg, #0071e3, #5ac8fa);
  color: #fff; font-size: 10px; font-weight: 600;
}
.reply-to-text { font-size: 12px; color: #0071e3; font-weight: 500; }
.reply-text { font-size: 13px; color: #1d1d1f; line-height: 1.5; flex: 1; min-width: 0; }
.reply-time { font-size: 11px; color: #aeaeb2; }

.reply-actions { display: flex; gap: 4px; margin-top: 4px; }
.reply-act-btn {
  display: inline-flex; align-items: center; gap: 3px;
  padding: 3px 8px; border-radius: 14px;
  border: none; background: rgba(0,0,0,0.04);
  color: #86868b; font-size: 11px; font-weight: 500;
  font-family: inherit; cursor: pointer; transition: all 0.18s ease;
}
.reply-act-btn:hover { background: rgba(0,0,0,0.08); color: #1d1d1f; }
.reply-act-btn.active { background: rgba(0,113,227,0.1); color: #0071e3; }
.reply-act-btn.reply-dislike.active { background: rgba(255,59,48,0.08); color: #ff3b30; }
.reply-act-btn.reply-delete:hover { background: rgba(255,59,48,0.1); color: #ff3b30; }

.no-replies { font-size: 12px; color: #aeaeb2; text-align: center; padding: 8px; }

.empty-state {
  display: flex; flex-direction: column; align-items: center; gap: 10px;
  padding: 60px 20px; text-align: center;
}
.empty-icon-wrap {
  width: 72px; height: 72px; border-radius: 20px;
  background: rgba(255,255,255,0.52);
  backdrop-filter: blur(20px);
  border: 0.5px solid rgba(255,255,255,0.85);
  box-shadow: 0 2px 20px rgba(0,0,0,0.08);
  display: flex; align-items: center; justify-content: center;
  color: #c7c7cc; margin-bottom: 6px;
}
.empty-title { font-size: 17px; font-weight: 600; color: #1d1d1f; }
.empty-desc { font-size: 14px; color: #86868b; }

/* 发起讨论弹窗 */
.dialog-overlay {
  position: fixed; inset: 0; z-index: 1000;
  background: rgba(0,0,0,0.3);
  backdrop-filter: blur(4px);
  display: flex; align-items: center; justify-content: center;
}
.dialog-card {
  width: 460px; max-width: 90vw;
  border-radius: 18px;
  background: rgba(255,255,255,0.95);
  backdrop-filter: saturate(200%) blur(40px);
  border: 0.5px solid rgba(255,255,255,0.85);
  box-shadow: 0 20px 60px rgba(0,0,0,0.2);
  overflow: hidden;
}
.dialog-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 18px 20px 0;
}
.dialog-title { font-size: 18px; font-weight: 700; color: #1d1d1f; letter-spacing: -0.02em; }
.dialog-close {
  width: 28px; height: 28px; border: none;
  background: rgba(0,0,0,0.06); border-radius: 50%;
  font-size: 18px; color: #86868b;
  cursor: pointer; transition: all 0.15s;
  display: flex; align-items: center; justify-content: center;
}
.dialog-close:hover { background: rgba(0,0,0,0.1); color: #1d1d1f; }

.dialog-body { padding: 20px; display: flex; flex-direction: column; gap: 18px; }
.dialog-section { display: flex; flex-direction: column; gap: 8px; }
.sec-title { font-size: 13px; font-weight: 600; color: #1d1d1f; }

.discussion-textarea {
  width: 100%; border: 0.5px solid rgba(0,0,0,0.1);
  border-radius: 10px; padding: 12px;
  font-size: 14px; font-family: inherit; line-height: 1.6;
  resize: none; background: rgba(255,255,255,0.6);
  color: #1d1d1f;
}
.discussion-textarea::placeholder { color: #aeaeb2; }
.discussion-textarea:focus {
  outline: none;
  border-color: rgba(0,113,227,0.4);
  box-shadow: 0 0 0 3px rgba(0,113,227,0.12);
}

.anonymous-toggle {
  display: flex; align-items: center; gap: 8px;
  cursor: pointer; user-select: none;
}
.toggle-box {
  width: 20px; height: 20px; border-radius: 6px;
  border: 1.5px solid rgba(0,0,0,0.15);
  display: flex; align-items: center; justify-content: center;
  transition: all 0.15s ease;
}
.toggle-box.active {
  background: #0071e3;
  border-color: #0071e3;
}
.toggle-label { font-size: 13px; color: #1d1d1f; }

.dialog-footer {
  display: flex; justify-content: flex-end; gap: 10px;
  padding: 0 20px 18px;
}
.dlg-btn {
  padding: 8px 22px; border-radius: 10px;
  border: none; font-size: 13px; font-weight: 600;
  font-family: inherit; cursor: pointer; transition: all 0.15s;
}
.dlg-cancel {
  background: rgba(0,0,0,0.06); color: #1d1d1f;
}
.dlg-cancel:hover { background: rgba(0,0,0,0.1); }
.dlg-confirm {
  background: #0071e3; color: #fff;
}
.dlg-confirm:hover { background: #0077ed; }

@media (max-width: 768px) {
  .header-title { font-size: 22px; }
  .filter-bar { flex-direction: column; }
  .review-card { padding: 14px; gap: 12px; }
  .avatar { width: 36px; height: 36px; font-size: 14px; }
  .review-content { font-size: 13px; }
  .dialog-card { width: 95vw; }
}
</style>
