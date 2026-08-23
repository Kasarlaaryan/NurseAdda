import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { PageHeader } from '../components/layouts/PageHeader';
import { Card, CardContent } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Input } from '../components/common/Input';
import { Badge } from '../components/common/Badge';
import { Avatar } from '../components/common/Avatar';
import { Table, TableHeader, TableBody, TableRow, TableHead, TableCell } from '../components/common/Table';
import { Loader } from '../components/common/Loader';
import { EmptyState } from '../components/common/EmptyState';
import { authService, UserResponse } from '../services/authService';
import { 
  Search, 
  UserCog, 
  Mail, 
  Building2, 
  ShieldCheck, 
  MoreHorizontal, 
  UserPlus,
  Shield,
  Clock
} from 'lucide-react';
import { Link } from 'react-router-dom';

/** Map backend role to display label */
function roleLabel(role: string) {
  return role.replace('ROLE_', '').replace('_', ' ');
}

function userDisplayName(u: UserResponse) {
  return `${u.firstName} ${u.lastName}`;
}

export const UserManagementPage: React.FC = () => {
  const [searchQuery, setSearchQuery] = useState('');

  const { data, isLoading } = useQuery({
    queryKey: ['admin-users'],
    queryFn: () => authService.getAllUsers(0, 100),
  });

  const users = data?.content || [];

  const filteredUsers = users.filter(u => {
    const fullName = userDisplayName(u);
    return (
      fullName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      u.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
      u.organizationName?.toLowerCase().includes(searchQuery.toLowerCase())
    );
  });

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-40">
        <Loader />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Portal User Management"
        description="Manage facility administrators, client coordinators, and internal system users."
        actions={
          <Link to="/users/create-admin">
            <Button variant="primary" size="sm" leftIcon={<UserPlus className="w-4 h-4" />}>
              Create Admin Account
            </Button>
          </Link>
        }
      />

      {/* Stats Summary */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <CardContent className="p-4 flex items-center gap-4">
            <div className="w-10 h-10 rounded-xl bg-amber-50 dark:bg-amber-950/40 text-amber-600 dark:text-amber-400 flex items-center justify-center">
              <UserCog className="w-5 h-5" />
            </div>
            <div>
              <p className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">Total Users</p>
              <p className="text-lg font-black text-slate-900 dark:text-slate-100">{users.length}</p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4 flex items-center gap-4">
            <div className="w-10 h-10 rounded-xl bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 dark:text-emerald-400 flex items-center justify-center">
              <Building2 className="w-5 h-5" />
            </div>
            <div>
              <p className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">Clients</p>
              <p className="text-lg font-black text-slate-900 dark:text-slate-100">
                {users.filter(u => u.role === 'ROLE_USER').length}
              </p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4 flex items-center gap-4">
            <div className="w-10 h-10 rounded-xl bg-purple-50 dark:bg-purple-950/40 text-purple-600 dark:text-purple-400 flex items-center justify-center">
              <Shield className="w-5 h-5" />
            </div>
            <div>
              <p className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">Admins</p>
              <p className="text-lg font-black text-slate-900 dark:text-slate-100">
                {users.filter(u => u.role === 'ROLE_ADMIN' || u.role === 'ROLE_SUPER_ADMIN').length}
              </p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4 flex items-center gap-4">
            <div className="w-10 h-10 rounded-xl bg-amber-50 dark:bg-amber-950/40 text-amber-600 dark:text-amber-400 flex items-center justify-center">
              <Clock className="w-5 h-5" />
            </div>
            <div>
              <p className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">Staff</p>
              <p className="text-lg font-black text-slate-900 dark:text-slate-100">
                {users.filter(u => u.role === 'ROLE_STAFF').length}
              </p>
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <div className="p-4 border-b border-slate-100 dark:border-slate-800 flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div className="relative w-full md:w-96">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
            <Input
              placeholder="Search by name, email, or facility..."
              className="pl-10"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
        </div>

        {filteredUsers.length === 0 ? (
          <EmptyState
            title="No users found"
            description="No portal users match your search criteria."
          />
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>User Profile</TableHead>
                <TableHead>Account Role</TableHead>
                <TableHead>Facility / Organization</TableHead>
                <TableHead>Access Level</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredUsers.map((u) => (
                <TableRow key={u.id}>
                  <TableCell>
                    <div className="flex items-center gap-3">
                      <Avatar name={userDisplayName(u)} size="sm" />
                      <div>
                        <p className="text-sm font-bold text-slate-900 dark:text-slate-100">{userDisplayName(u)}</p>
                        <p className="text-[11px] text-slate-500 flex items-center gap-1">
                          <Mail className="w-3 h-3" />
                          {u.email}
                        </p>
                      </div>
                    </div>
                  </TableCell>
                  <TableCell>
                    <Badge 
                      variant={
                        u.role === 'ROLE_SUPER_ADMIN' ? 'danger' : 
                        u.role === 'ROLE_ADMIN' ? 'warning' : 
                        u.role === 'ROLE_STAFF' ? 'neutral' :
                        'info'
                      } 
                      size="sm"
                    >
                      {roleLabel(u.role)}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center gap-2">
                      <Building2 className="w-3.5 h-3.5 text-slate-400" />
                      <span className="text-xs font-medium">{u.organizationName || 'NurseAdda Internal'}</span>
                    </div>
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center gap-2">
                      <ShieldCheck className="w-3.5 h-3.5 text-emerald-500" />
                      <span className="text-xs text-slate-600 dark:text-slate-400">Full Portal Access</span>
                    </div>
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex items-center justify-end gap-2">
                      <Button variant="ghost" size="sm" disabled>Edit</Button>
                      <Button variant="ghost" size="sm" className="h-8 w-8 p-0" disabled>
                        <MoreHorizontal className="w-4 h-4 text-slate-400" />
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </Card>
    </div>
  );
};
