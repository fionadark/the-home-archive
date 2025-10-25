require('@testing-library/jest-dom')
const fetchMock = require('jest-fetch-mock')

// Enable fetch mock globally
fetchMock.enableMocks()

// Mock localStorage
const localStorageMock = {
  getItem: jest.fn(),
  setItem: jest.fn(),
  removeItem: jest.fn(),
  clear: jest.fn()
}

global.localStorage = localStorageMock

// Mock sessionStorage
const sessionStorageMock = {
  getItem: jest.fn(),
  setItem: jest.fn(),
  removeItem: jest.fn(),
  clear: jest.fn()
}

global.sessionStorage = sessionStorageMock

// Mock window.location
delete window.location
window.location = {
  href: 'http://localhost:3000/',
  hostname: 'localhost',
  port: '3000',
  protocol: 'http:',
  pathname: '/',
  search: '',
  hash: '',
  assign: jest.fn(),
  replace: jest.fn(),
  reload: jest.fn()
}

// Mock window.alert, confirm, and prompt
global.alert = jest.fn()
global.confirm = jest.fn(() => true)
global.prompt = jest.fn()

// Mock IntersectionObserver
global.IntersectionObserver = class IntersectionObserver {
  constructor() {}
  disconnect() {}
  observe() {}
  unobserve() {}
}

// Mock ResizeObserver
global.ResizeObserver = class ResizeObserver {
  constructor() {}
  disconnect() {}
  observe() {}
  unobserve() {}
}

// Setup for each test
beforeEach(() => {
  // Clear all mocks
  jest.clearAllMocks()
  
  // Reset fetch mock
  fetch.resetMocks()
  
  // Clear localStorage and sessionStorage
  localStorage.clear()
  sessionStorage.clear()
  
  // Reset window.location
  window.location.href = 'http://localhost:3000/'
  window.location.pathname = '/'
  window.location.search = ''
  window.location.hash = ''
})

// Global test utilities
global.waitFor = (callback, options = {}) => {
  return new Promise((resolve, reject) => {
    const { timeout = 1000, interval = 50 } = options
    const startTime = Date.now()
    
    const check = () => {
      try {
        const result = callback()
        if (result) {
          resolve(result)
        } else if (Date.now() - startTime >= timeout) {
          reject(new Error('waitFor timeout'))
        } else {
          setTimeout(check, interval)
        }
      } catch (error) {
        if (Date.now() - startTime >= timeout) {
          reject(error)
        } else {
          setTimeout(check, interval)
        }
      }
    }
    
    check()
  })
}

// Mock API responses for common endpoints
global.mockApiResponse = (endpoint, response, status = 200) => {
  fetch.mockResponseOnce(JSON.stringify(response), {
    status,
    headers: { 'Content-Type': 'application/json' }
  })
}

// Helper to create mock user data
global.createMockUser = (overrides = {}) => ({
  id: 1,
  email: 'test@example.com',
  firstName: 'Test',
  lastName: 'User',
  role: 'USER',
  emailVerified: true,
  createdAt: '2023-01-01T00:00:00Z',
  ...overrides
})

// Helper to create mock book data
global.createMockBook = (overrides = {}) => ({
  id: 1,
  title: 'Test Book',
  author: 'Test Author',
  isbn: '978-0123456789',
  description: 'A test book description',
  publicationYear: 2023,
  publisher: 'Test Publisher',
  pageCount: 300,
  category: { id: 1, name: 'Fiction' },
  averageRating: 4.5,
  ratingCount: 10,
  ...overrides
})

// Console error handler for tests
const originalError = console.error
beforeAll(() => {
  console.error = (...args) => {
    if (
      typeof args[0] === 'string' &&
      args[0].includes('Warning: ReactDOM.render is no longer supported')
    ) {
      return
    }
    originalError.call(console, ...args)
  }
})

afterAll(() => {
  console.error = originalError
})