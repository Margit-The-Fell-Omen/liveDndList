// src/App.tsx

import {BrowserRouter, Navigate, Route, Routes} from 'react-router-dom';
import {AuthProvider, useAuth} from './context/AuthContext';
import {ThemeProvider} from './context/ThemeContext';
import {CharacterProvider} from './context/CharacterContext';
import {Layout} from './components/layout/Layout';
import {MainPage} from './pages/MainPage';
import {AuthPage} from './pages/AuthPage';

function ProtectedRoute({children}: { children: React.ReactNode }) {
  const {isAuthenticated, loading} = useAuth();

  if (loading) {
    return <div>Loading session...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/auth" replace/>;
  }

  return <>{children}</>;
}

function AppRoutes() {
  return (
      <Routes>
        <Route path="/auth" element={<AuthPage/>}/>

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

        <Route path="*" element={<Navigate to="/" replace/>}/>
      </Routes>
  );
}

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
