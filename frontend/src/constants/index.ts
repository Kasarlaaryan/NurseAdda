import { UserRole } from '../types';
import {
  LayoutDashboard,
  Users,
  UserCog,
  Building2,
  ClipboardList,
  Clock,
  FileText,
  CreditCard,
  BarChart3,
  Bell,
  Settings,
  CalendarCheck,
  ShieldCheck,
  PlusCircle,
} from 'lucide-react';

export interface NavItem {
  title: string;
  href: string;
  icon: any;
  badge?: string;
  roles: UserRole[];
}

export const USER_ROLES_CONFIG: Record<UserRole, { label: string; description: string; color: string }> = {
  ROLE_SUPER_ADMIN: {
    label: 'Super Admin',
    description: 'Full system control, global configuration & management',
    color: 'bg-purple-100 text-purple-800 dark:bg-purple-900/40 dark:text-purple-300'
  },
  ROLE_ADMIN: {
    label: 'Admin',
    description: 'Daily staffing operations, requests & invoicing',
    color: 'bg-blue-100 text-blue-800 dark:bg-blue-900/40 dark:text-blue-300'
  },
  ROLE_STAFF: {
    label: 'Healthcare Staff',
    description: 'Nurses, Caregivers & Technicians shift management',
    color: 'bg-emerald-100 text-emerald-800 dark:bg-emerald-900/40 dark:text-emerald-300'
  },
  ROLE_USER: {
    label: 'Regular User',
    description: 'Individual users and organization members',
    color: 'bg-amber-100 text-amber-800 dark:bg-amber-900/40 dark:text-amber-300'
  }
};

export const NAVIGATION_ITEMS: NavItem[] = [
  {
    title: 'Dashboard',
    href: '/dashboard',
    icon: LayoutDashboard,
    roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_STAFF', 'ROLE_USER']
  },
  {
    title: 'User Management',
    href: '/users',
    icon: UserCog,
    roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN']
  },
  {
    title: 'Staff Directory',
    href: '/staff',
    icon: Users,
    roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN']
  },
  {
    title: 'Client Organizations',
    href: '/clients',
    icon: Building2,
    roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN']
  },
  {
    title: 'Staff Verifications',
    href: '/verifications',
    icon: ShieldCheck,
    roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN']
  },
  {
    title: 'Staffing Requests',
    href: '/requests',
    icon: ClipboardList,
    roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_USER']
  },
  {
    title: 'New Staffing Request',
    href: '/requests/create',
    icon: PlusCircle,
    roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_USER']
  },
  {
    title: 'Assignments',
    href: '/assignments',
    icon: CalendarCheck,
    roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_STAFF', 'ROLE_USER']
  },
  {
    title: 'Attendance & Timesheets',
    href: '/attendance',
    icon: Clock,
    roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_STAFF']
  },
  {
    title: 'Invoices & Billing',
    href: '/invoices',
    icon: FileText,
    roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_USER']
  },
  {
    title: 'Payments Tracker',
    href: '/payments',
    icon: CreditCard,
    roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN']
  },
  {
    title: 'Reports & Analytics',
    href: '/reports',
    icon: BarChart3,
    roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN']
  },
  {
    title: 'Notifications',
    href: '/notifications',
    icon: Bell,
    roles: ['ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_STAFF', 'ROLE_USER']
  },
  {
    title: 'Settings & Master Data',
    href: '/settings',
    icon: Settings,
    roles: ['ROLE_SUPER_ADMIN']
  }
];
