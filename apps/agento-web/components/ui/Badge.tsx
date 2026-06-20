type BadgeVariant = 'draft' | 'approved' | 'rejected' | 'active' | 'default';

const styles: Record<BadgeVariant, string> = {
  draft: 'bg-yellow-100 text-yellow-800',
  approved: 'bg-green-100 text-green-800',
  rejected: 'bg-red-100 text-red-800',
  active: 'bg-blue-100 text-blue-800',
  default: 'bg-gray-100 text-gray-800',
};

function toVariant(status: string): BadgeVariant {
  const lower = status?.toLowerCase() as BadgeVariant;
  return lower in styles ? lower : 'default';
}

export default function Badge({ status }: { status: string }) {
  const variant = toVariant(status);
  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${styles[variant]}`}>
      {status}
    </span>
  );
}
