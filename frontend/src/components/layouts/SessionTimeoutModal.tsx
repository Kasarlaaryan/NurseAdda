import React from 'react';
import { useAppStore } from '../../store/useAppStore';
import { useAuthStore } from '../../store/useAuthStore';
import { Modal } from '../common/Modal';
import { Button } from '../common/Button';
import { Clock, Lock } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export const SessionTimeoutModal: React.FC = () => {
  const navigate = useNavigate();
  const { sessionTimeoutModalOpen, closeSessionTimeoutModal } = useAppStore();
  const { logout } = useAuthStore();

  const handleExtend = () => {
    closeSessionTimeoutModal();
  };

  const handleLogout = () => {
    closeSessionTimeoutModal();
    logout();
    navigate('/login');
  };

  return (
    <Modal
      isOpen={sessionTimeoutModalOpen}
      onClose={closeSessionTimeoutModal}
      title={
        <div className="flex items-center gap-2 text-rose-600 dark:text-rose-400">
          <Clock className="w-5 h-5" />
          <span>Security Session Expiry Warning</span>
        </div>
      }
      maxWidth="md"
      footer={
        <>
          <Button variant="outline" size="sm" onClick={handleLogout}>
            Log Out Now
          </Button>
          <Button variant="primary" size="sm" onClick={handleExtend} leftIcon={<Lock className="w-3.5 h-3.5" />}>
            Extend My Session
          </Button>
        </>
      }
    >
      <div className="space-y-3 text-xs text-slate-600 dark:text-slate-300">
        <p className="font-medium text-slate-900 dark:text-slate-100 text-sm">
          Your active session is about to expire due to inactive security threshold (15 mins).
        </p>
        <p>
          To safeguard sensitive healthcare data and HIPAA compliance standards, your portal will automatically lock if no activity is detected in the next 60 seconds.
        </p>
      </div>
    </Modal>
  );
};
