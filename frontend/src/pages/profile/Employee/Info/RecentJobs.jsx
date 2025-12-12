import { useEffect, useState } from "react";
import InfoLayout from "./InfoLayout.jsx";
import { useEmployeeProfile } from "../EmployeeProfileContext.jsx";
import JobCard from "../JobOffers/JobCard.jsx";
import { listEmployeeJobs } from "../../../../api/profile/profileJobs.js";
import { formatName } from "../utils/profileUtils.js";
import Button from "../../../../components/ui/Button/Button.jsx";

export default function RecentJobs() {
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const { profile, refreshProfile, jobsData, setJobsData } = useEmployeeProfile();
  const [displayName, setDisplayName] = useState("");

  useEffect(() => {
    let isMounted = true;

    async function loadJobs() {
      try {
        if (jobsData) {
          setJobs(jobsData);
          return;
        }
        const data = await listEmployeeJobs();
        if (!isMounted) return;
        setJobs(Array.isArray(data) ? data : []);
        setJobsData(Array.isArray(data) ? data : []);
      } catch (err) {
        if (isMounted) setError(err.message || "Não foi possível carregar os trabalhos.");
      } finally {
        if (isMounted) setLoading(false);
      }
    }

    loadJobs();

    if (!profile) {
      refreshProfile().then((data) => {
        if (isMounted && data) {
          setDisplayName(formatName(data.firstName, data.lastName));
        }
      });
    } else {
      setDisplayName(formatName(profile.firstName, profile.lastName));
    }

    return () => {
      isMounted = false;
    };
  }, [profile, refreshProfile, jobsData, setJobsData]);

  return (
    <InfoLayout name={displayName}>
      <div className="mt-6 rounded-xl border border-base-300 bg-base-100 shadow min-h-[55vh]">
        <div className="p-4 md:p-6 space-y-4">
          {error && (
            <div className="alert alert-error text-sm" role="alert">
              {error}
            </div>
          )}

          {loading ? (
            <SkeletonList />
          ) : jobs.length === 0 ? (
            <EmptyState />
          ) : (
            <div className="space-y-4 max-w-3xl mx-auto">
              {jobs.map((job) => (
                <JobCard
                  key={job.requestId ?? job.id}
                  job={job}
                  actionSlot={<Button label="Ver contrato" variant="primary" fullWidth={false} />}
                />
              ))}
            </div>
          )}
        </div>
      </div>
    </InfoLayout>
  );
}

function SkeletonList() {
  return (
    <div className="animate-pulse space-y-3">
      <div className="h-20 bg-base-200 rounded-xl" />
      <div className="h-20 bg-base-200 rounded-xl" />
      <div className="h-20 bg-base-200 rounded-xl" />
    </div>
  );
}

function EmptyState() {
  return (
    <div className="text-center text-base-content/70 py-12 border border-dashed border-base-300 rounded-xl">
      Ainda não existem registos de trabalhos concluídos.
    </div>
  );
}
