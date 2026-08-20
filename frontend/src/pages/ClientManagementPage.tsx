import React, { useState } from 'react';
import { PageHeader } from '../components/layouts/PageHeader';
import { Card, CardHeader, CardTitle, CardContent } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Input } from '../components/common/Input';
import { Badge } from '../components/common/Badge';
import { Table, TableHeader, TableBody, TableRow, TableHead, TableCell } from '../components/common/Table';
import { Modal } from '../components/common/Modal';
import { useToast } from '../components/common/Toast';
import { MOCK_CLIENT_ORGS } from '../services/mockData';
import { ClientOrganization, OrganizationType } from '../types';
import { Building2, Plus, Search, Phone, Mail, MapPin, FileSpreadsheet } from 'lucide-react';

export const ClientManagementPage: React.FC = () => {
  const { showToast } = useToast();
  const [clients, setClients] = useState<ClientOrganization[]>(MOCK_CLIENT_ORGS);
  const [searchQuery, setSearchQuery] = useState('');
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);

  // New Client Form
  const [name, setName] = useState('');
  const [type, setType] = useState<OrganizationType>('Hospital');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [address, setAddress] = useState('');
  const [primaryContact, setPrimaryContact] = useState('');

  const filteredClients = clients.filter(
    (c) =>
      c.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      c.type.toLowerCase().includes(searchQuery.toLowerCase()) ||
      c.primaryContactName.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const handleAddClient = (e: React.FormEvent) => {
    e.preventDefault();
    const newOrg: ClientOrganization = {
      id: `org-${Date.now()}`,
      name,
      type,
      email,
      phone,
      address,
      city: 'Metropolis',
      branchesCount: 1,
      activeRequestsCount: 1,
      activeStaffCount: 0,
      primaryContactName: primaryContact,
      primaryContactRole: 'Healthcare Director',
      contractStatus: 'Active',
      totalBilled: 0,
      outstandingBalance: 0
    };

    setClients([newOrg, ...clients]);
    setIsAddModalOpen(false);
    showToast('success', 'Client Organization Registered', `${name} has been enrolled into NurseAdda.`);
    setName('');
    setEmail('');
    setPhone('');
    setAddress('');
    setPrimaryContact('');
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="Client Organizations & Hospitals"
        description="Oversee partner hospitals, specialized clinics, corporate clients, active staffing contracts & balances."
        actions={
          <Button variant="primary" size="sm" leftIcon={<Plus className="w-4 h-4" />} onClick={() => setIsAddModalOpen(true)}>
            Onboard New Client
          </Button>
        }
      />

      <div className="flex justify-between items-center">
        <div className="w-full md:w-80">
          <Input
            placeholder="Search hospitals or contact persons..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            leftIcon={<Search className="w-4 h-4 text-slate-400" />}
          />
        </div>
      </div>

      <Card>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Organization Name</TableHead>
              <TableHead>Type & Branches</TableHead>
              <TableHead>Primary Contact</TableHead>
              <TableHead>Active Staff</TableHead>
              <TableHead>Contract Status</TableHead>
              <TableHead>Outstanding Balance</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filteredClients.map((client) => (
              <TableRow key={client.id}>
                <TableCell>
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-xl bg-blue-100 dark:bg-blue-900/60 text-blue-700 dark:text-blue-300 font-bold flex items-center justify-center shrink-0">
                      <Building2 className="w-5 h-5" />
                    </div>
                    <div>
                      <p className="font-bold text-slate-900 dark:text-slate-100">{client.name}</p>
                      <p className="text-xs text-slate-500 flex items-center gap-1">
                        <MapPin className="w-3 h-3" /> {client.address}
                      </p>
                    </div>
                  </div>
                </TableCell>
                <TableCell>
                  <Badge variant="info" size="sm">{client.type}</Badge>
                  <p className="text-xs text-slate-500 mt-1">{client.branchesCount} Active Branches</p>
                </TableCell>
                <TableCell>
                  <p className="font-semibold text-slate-900 dark:text-slate-100">{client.primaryContactName}</p>
                  <p className="text-xs text-slate-500">{client.primaryContactRole}</p>
                </TableCell>
                <TableCell>
                  <p className="font-extrabold text-teal-600 dark:text-teal-400">{client.activeStaffCount} Staff Assigned</p>
                  <p className="text-xs text-slate-500">{client.activeRequestsCount} Open Requests</p>
                </TableCell>
                <TableCell>
                  <Badge variant={client.contractStatus === 'Active' ? 'success' : 'warning'} size="sm" dot>
                    {client.contractStatus}
                  </Badge>
                </TableCell>
                <TableCell>
                  <p className="font-bold text-slate-900 dark:text-slate-100">${client.outstandingBalance.toLocaleString()}</p>
                  <p className="text-[11px] text-slate-400">Total Billed: ${client.totalBilled.toLocaleString()}</p>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Card>

      <Modal
        isOpen={isAddModalOpen}
        onClose={() => setIsAddModalOpen(false)}
        title="Onboard New Healthcare Client Organization"
        maxWidth="lg"
      >
        <form onSubmit={handleAddClient} className="space-y-4">
          <Input
            label="Organization Legal Name"
            placeholder="e.g. Metro Medical Center"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
          />
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="text-xs font-semibold uppercase tracking-wider text-slate-700 dark:text-slate-300 block mb-1">
                Organization Type
              </label>
              <select
                className="w-full rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 text-sm p-2.5"
                value={type}
                onChange={(e) => setType(e.target.value as OrganizationType)}
              >
                <option value="Hospital">Hospital</option>
                <option value="Clinic">Clinic</option>
                <option value="Corporate Healthcare Organization">Corporate Healthcare</option>
              </select>
            </div>
            <Input
              label="Primary Contact Person Name"
              placeholder="Dr. Evelyn Reed"
              value={primaryContact}
              onChange={(e) => setPrimaryContact(e.target.value)}
              required
            />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Billing Email Address"
              type="email"
              placeholder="billing@hospital.org"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
            <Input
              label="Contact Phone"
              placeholder="+1 (555) 000-0000"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              required
            />
          </div>
          <Input
            label="Main Facility Address"
            placeholder="450 Healthcare Blvd, Suite 100"
            value={address}
            onChange={(e) => setAddress(e.target.value)}
            required
          />
          <div className="pt-3 flex justify-end gap-2">
            <Button variant="ghost" size="sm" type="button" onClick={() => setIsAddModalOpen(false)}>
              Cancel
            </Button>
            <Button variant="primary" size="sm" type="submit">
              Complete Onboarding
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
