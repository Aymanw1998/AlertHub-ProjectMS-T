import { Navigate, Route, Routes } from 'react-router-dom'
import { DashboardLayout } from './components/layout/DashboardLayout'
import { ProtectedRoute } from './auth/ProtectedRoute'
import { DashboardPage } from './pages/DashboardPage'
import { ActionsPage } from './pages/ActionsPage'
import { EvaluationPage } from './pages/EvaluationPage'
import { LoaderPage } from './pages/LoaderPage'
import { LoginPage } from './pages/LoginPage'
import { LogsPage } from './pages/LogsPage'
import { MetricsPage } from './pages/MetricsPage'
import { NotificationsPage } from './pages/NotificationsPage'
import { ProcessorPage } from './pages/ProcessorPage'
import { SignupPage } from './pages/SignupPage'
import { UsersPage } from './pages/UsersPage'

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
        <Route path="loader" element={<LoaderPage />} />
        <Route path="metrics" element={<MetricsPage />} />
        <Route path="actions" element={<ActionsPage />} />
        <Route path="processor" element={<ProcessorPage />} />
        <Route path="notifications" element={<NotificationsPage />} />
        <Route path="evaluation" element={<EvaluationPage />} />
        <Route path="users" element={<UsersPage />} />
        <Route path="logs" element={<LogsPage />} />
      </Route>

      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  )
}

export default App
