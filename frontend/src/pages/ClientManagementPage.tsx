import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { PageHeader } from '../components/layouts/PageHeader';
import { Card, CardContent } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Input } from '../components/common/Input';
import { Badge } from '../components/common/Badge';
import { Table, TableHeader, TableBody, TableRow, TableHead, TableCell } from '../components/common/Table';
import { Loader } from '../components/common/Loader';
import { EmptyState } from '../components/common/EmptyState';
import { useToast } from '../components/common/Toast';
import { authService, UserResponse } from '../services/authService';
import { Building2, Search, Phone, Mail, UserCheck } from 'lucide-react';

/** Map backend role to display info */
function roleDisplay(role: string) {
  const map: Record<string, { label: string; variant: 'success' | 'warning' | 'info' | 'danger' | 'neutral' }> = {
    ROLE_USER: { label: 'Client', variant: 'info' },
    ROLE_STAFF: { label: 'Staff', variant: 'success' },
    ROLE_ADMIN: { label: 'Admin', variant: 'warning' },
    ROLE_SUPER_ADMIN: { label: 'Super Admin', variant: 'danger' },
  };
  return map[role] || { label: role, variant: 'neutral' as const };
}

export const ClientManagementPage: React.FC = () => {
  const { showToast } = useToast();
  const [searchQuery, setSearchQuery] = useState('');

  const { data, isLoading } = useQuery({
    queryKey: ['admin-users'],
    queryFn: () => authService.getAllUsers(0, 100),
  });

  const users = data?.content || [];

  // Filter to show client (ROLE_USER) accounts by default, but allow search across all
  const filteredUsers = users.filter((u) => {
    const fullName = `${u.firstName} ${u.lastName}`.toLowerCase();
    const matchesSearch =
      fullName.includes(searchQuery.toLowerCase()) ||
      u.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (u.phone && u.phone.includes(searchQuery));
    return matchesSearch;
  });

  const clientCount = users.filter((u) => u.role === 'ROLE_USER').length;
  const staffCount = users.filter((u) => u.role === 'ROLE_STAFF').length;
  const adminCount = users.filter((u) => u.role === 'ROLE_ADMIN' || u.role === 'ROLE_SUPER_ADMIN').length;

  return (
    <div className="space-y-6">
      <PageHeader
        title="Client & User Management"
        description="Manage facility administrators, client coordinators, and internal system users."
      />

      {/* Summary Stats */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card className="border-l-4 border-l-amber-500">
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs font-black uppercase tracking-widest text-slate-400">Client Accounts</p>
                <h3 className="text-2xl font-black text-slate-900 dark:text-white mt-1">{clientCount}</h3>
              </div>
              <div className="p-3 rounded-xl bg-amber-50 dark:bg-amber-500/10 text-amber-600">
                <Building2 className="w-6 h-6" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="border-l-4 border-l-emerald-500">
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs font-black uppercase tracking-widest text-slate-400">Staff Accounts</p>
                <h3 className="text-2xl font-black text-slate-900 dark:text-white mt-1">{staffCount}</h3>
              </div>
              <div className="p-3 rounded-xl bg-emerald-50 dark:bg-emerald-500/10 text-emerald-600">
                <UserCheck className="w-6 h-6" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="border-l-4 border-l-amber-500">
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs font-black uppercase tracking-widest text-slate-400">Admin Accounts</p>
                <h3 className="text-2xl font-black text-slate-900 dark:text-white mt-1">{adminCount}</h3>
              </div>
              <div className="p-3 rounded-xl bg-amber-50 dark:bg-amber-500/10 text-amber-600">
                <Building2 className="w-6 h-6" />
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      <div className="flex justify-between items-center">
        <div className="w-full md:w-80">
          <Input
            placeholder="Search by name, email, or phone..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            leftIcon={<Search className="w-4 h-4 text-slate-400" />}
          />
        </div>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center h-40">
          <Loader />
        </div>
      ) : filteredUsers.length === 0 ? (
        <EmptyState
          title="No users found"
          description="Users will appear here once they register through the portal."
        />
      ) : (
        <Card>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>User</TableHead>
                <TableHead>Contact</TableHead>
                <TableHead>Role</TableHead>
                <TableHead>User ID</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredUsers.map((user) => {
                const { label: roleLabel, variant } = roleDisplay(user.role);
                return (
                  <TableRow key={user.id}>
                    <TableCell>
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-xl bg-amber-100 dark:bg-amber-900/60 text-amber-700 dark:text-amber-300 font-bold flex items-center justify-center shrink-0 text-sm">
                          {user.firstName?.[0]}{user.lastName?.[0]}
                        </div>
                        <div>
                          <p className="font-bold text-slate-900 dark:text-slate-100">{user.firstName} {user.lastName}</p>
                          <p className="text-xs text-slate-500 flex items-center gap-1">
                            <Mail className="w-3 h-3" /> {user.email}
                          </p>
                        </div>
                      </div>
                    </TableCell>
                    <TableCell>
                      <span className="text-xs flex items-center gap-1">
                        <Phone className="w-3 h-3 text-slate-400" />
                        {user.phone || '—'}
                      </span>
                    </TableCell>
                    <TableCell>
                      <Badge variant={variant} size="sm" dot>
                        {roleLabel}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      <span className="text-xs text-slate-400 font-mono">#{user.id}</span>
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        </Card>
      )}
    </div>
  );
};
