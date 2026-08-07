import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import Home from './pages/Home';
import Machines from './pages/Machines';
import MachineDetail from './pages/MachineDetail';
import Diagnose from './pages/Diagnose';
import DiagnosisResult from './pages/DiagnosisResult';
import Chat from './pages/Chat';
import AdminDashboard from './pages/AdminDashboard';
import AdminReports from './pages/AdminReports';
import Learn from './pages/Learn';
import Announcement from './pages/Announcement';
import CallExperts from './pages/CallExperts';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<Home />} />
          <Route path="/announcements" element={<Announcement />} />
          <Route path="/machines" element={<Machines />} />
          <Route path="/machines/:id" element={<MachineDetail />} />
          <Route path="/diagnose" element={<Diagnose />} />
          <Route path="/diagnosis/:id" element={<DiagnosisResult />} />
          <Route path="/chat/:reportId" element={<Chat />} />
          <Route path="/learn" element={<Learn />} />
          <Route path="/call-experts" element={<CallExperts />} />
          <Route path="/admin" element={<Navigate to="/admin/dashboard" replace />} />
          <Route path="/admin/dashboard" element={<AdminDashboard />} />
          <Route path="/admin/reports" element={<AdminReports />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
