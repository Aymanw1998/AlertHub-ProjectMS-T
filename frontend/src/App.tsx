import { Navigate, Route, Routes } from 'react-router-dom'
import { DashboardLayout } from './components/layout/DashboardLayout'
import { ProtectedRoute } from './auth/ProtectedRoute'
import { DashboardPage } from './pages/DashboardPage'
import { LoginPage } from './pages/LoginPage'
import { PlaceholderPage } from './pages/PlaceholderPage'
import { SignupPage } from './pages/SignupPage'

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />

      <Route
        path="/"
        element={
          <ProtectedRoute>
            <DashboardLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<DashboardPage />} />
        <Route path="loader" element={<PlaceholderPage title="Loader" />} />
        <Route path="metrics" element={<PlaceholderPage title="Metrics" />} />
        <Route path="actions" element={<PlaceholderPage title="Actions" />} />
        <Route path="logs" element={<PlaceholderPage title="Logs" />} />
        <Route
          path="evaluation"
          element={<PlaceholderPage title="Evaluation" />}
        />
        <Route path="users" element={<PlaceholderPage title="Users" />} />
      </Route>

      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  )
}

export default App
