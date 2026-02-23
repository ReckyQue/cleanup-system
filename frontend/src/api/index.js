import request from '@/utils/request'

export const adminApi = {
  login: (data) => request.post('/admin/login', data)
}

export const chargingStationApi = {
  getAll: () => request.get('/charging-stations'),
  getById: (id) => request.get(`/charging-stations/${id}`),
  add: (data) => request.post('/charging-stations', data),
  delete: (id) => request.delete(`/charging-stations/${id}`),
  search: (name) => request.get('/charging-stations/search', { params: { name } })
}

export const cleaningCarApi = {
  getAll: () => request.get('/cleaning-cars'),
  getById: (id) => request.get(`/cleaning-cars/${id}`),
  add: (data) => request.post('/cleaning-cars', data),
  delete: (id) => request.delete(`/cleaning-cars/${id}`),
  search: (name) => request.get('/cleaning-cars/search', { params: { name } }),
  assignTask: (id, coordinates) => request.post(`/cleaning-cars/${id}/assign-task`, coordinates)
}

export const taskApi = {
  getAll: () => request.get('/tasks'),
  getCompleted: () => request.get('/tasks/completed'),
  getById: (id) => request.get(`/tasks/${id}`),
  add: (data) => request.post('/tasks', data),
  complete: (id) => request.post(`/tasks/${id}/complete`),
  deleteAll: () => request.delete('/tasks'),
  deletePending: () => request.delete('/tasks/pending'),
  deleteInProgress: () => request.delete('/tasks/in-progress'),
  deleteCompleted: () => request.delete('/tasks/completed')
}
