// src/App.tsx

import {BrowserRouter, Navigate, Route, Routes} from 'react-router-dom';
import {AuthProvider, useAuth} from './context/AuthContext';
import {ThemeProvider} from './context/ThemeContext';
import {CharacterProvider} from './context/CharacterContext';
import {Layout} from './components/layout/Layout';
import {MainPage} from './pages/MainPage';
import {AuthPage} from './pages/AuthPage';

/**
 * A wrapper component that protects routes requiring authentication.
 * If the user is not authenticated, it redirects them to the login page.
 */
function ProtectedRoute({children}: { children: React.ReactNode }) {
  const {isAuthenticated, loading} = useAuth();

  if (loading) {
    // Show a global loader while checking auth status
    return <div>Loading session...</div>;
  }

  if (!isAuthenticated) {
    // Redirect to the login page, saving the location they tried to access
    return <Navigate to="/auth" replace/>;
  }

  // If authenticated, render the requested component
  return <>{children}</>;
}

/**
 * Defines the application's routes.
 */
function AppRoutes() {
  return (
      <Routes>
        {/* Public route for authentication */}
        <Route path="/auth" element={<AuthPage/>}/>

        {/* Protected route for the main application */}
        <Route
            path="/"
            element={
              <ProtectedRoute>
                <Layout>
                  <MainPage/>
                </Layout>
              </ProtectedRoute>
            }
        />

        {/* Optional: Add a catch-all redirect for any other path */}
        <Route path="*" element={<Navigate to="/" replace/>}/>
      </Routes>
  );
}

/**
 * The main App component, responsible for setting up providers.
 */
function App() {
  return (
      <BrowserRouter>
        <ThemeProvider>
          <AuthProvider>
            <CharacterProvider>
              <AppRoutes/>
            </CharacterProvider>
          </AuthProvider>
        </ThemeProvider>
      </BrowserRouter>
  );
}

export default App;
