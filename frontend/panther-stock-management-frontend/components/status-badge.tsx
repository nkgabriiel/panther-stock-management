import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";
import type { StatusAnuncio } from "@/lib/types";

const config: Record<StatusAnuncio, { label: string; className: string }> = {
  PODE_ANUNCIAR: {
    label: "PODE ANUNCIAR",
    className: "bg-emerald-100 text-emerald-800 dark:bg-emerald-900/40 dark:text-emerald-300",
  },
  NO_LIMITE: {
    label: "NO LIMITE",
    className: "bg-muted text-muted-foreground",
  },
  REDUZIR: {
    label: "REDUZIR",
    className: "bg-amber-100 text-amber-800 dark:bg-amber-900/40 dark:text-amber-300",
  },
};

export function StatusBadge({
  status,
  folga,
  className,
}: {
  status: StatusAnuncio;
  folga?: number;
  className?: string;
}) {
  const { label, className: statusClassName } = config[status];
  const suffix = status === "REDUZIR" && folga !== undefined ? ` ${Math.abs(folga)}` : "";

  return (
    <Badge className={cn("font-semibold tracking-wide", statusClassName, className)}>
      {label}
      {suffix}
    </Badge>
  );
}
