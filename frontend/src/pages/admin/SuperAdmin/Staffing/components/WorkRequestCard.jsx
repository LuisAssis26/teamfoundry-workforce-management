import React from 'react';
import PropTypes from 'prop-types';
import Button from '../../../../../components/ui/Button/Button.jsx';

const WorkRequestCard = ({
                           company,
                           teamName,
                           description,
                           state,
                           responsibleAdminName,
                           startDate,
                           endDate,
                           createdAt,
                           onAssignAdmin,
                         }) => {
  const getStatus = () => {
    switch (state) {
      case 'PENDING':
        return { label: 'Pendente', color: 'text-warning' };
      case 'COMPLETE':
        return { label: 'Concluida', color: 'text-success' };
      case 'INCOMPLETE':
        return { label: 'Incompleta', color: 'text-error' };
      default:
        return { label: 'N/A', color: 'text-neutral' };
    }
  };

  const formatDate = (dateValue) => {
    if (!dateValue) return 'N/A';
    const parsedDate = new Date(dateValue);
    return Number.isNaN(parsedDate.getTime())
        ? 'N/A'
        : parsedDate.toLocaleDateString('pt-PT');
  };

  const { label: statusLabel, color: statusColor } = getStatus();
  const isComplete = state === 'COMPLETE';
  const buttonLabel = isComplete
      ? 'Requisição concluída'
      : !responsibleAdminName
          ? 'Atribuir Responsavel'
          : 'Modificar Responsavel';
  const buttonVariant = isComplete ? 'ghost' : !responsibleAdminName ? 'success' : 'primary';

  return (
      <div className="bg-base-100 rounded-2xl shadow-sm p-6 mb-4 border border-base-200">
        <div className="flex justify-between items-start gap-6">
          <div className="flex-1">
            <div className="grid grid-cols-2 gap-6">
              <div className="space-y-2">
                <p className="text-label font-medium text-base-content">
                  <span className="font-semibold">Empresa:</span> {company?.name || 'N/A'}
                </p>
                <p className="text-label text-base-content">
                  <span className="font-semibold">Equipe:</span> {teamName || 'N/A'}
                </p>
                <p className="text-label text-base-content">
                  <span className="font-semibold">Descricao:</span> {description || 'N/A'}
                </p>
                <p className="text-label text-base-content">
                  <span className="font-semibold">Responsavel:</span>{' '}
                  {responsibleAdminName || 'N/A'}
                </p>
              </div>

              <div className="space-y-2">
                <p className="text-label text-base-content">
                  <span className="font-semibold">Data de inicio:</span> {formatDate(startDate)}
                </p>
                <p className="text-label text-base-content">
                  <span className="font-semibold">Data de finalizacao:</span> {formatDate(endDate)}
                </p>
                <p className="text-label text-base-content">
                  <span className="font-semibold">Criado em:</span> {formatDate(createdAt)}
                </p>
                <p className="text-label text-base-content">
                  <span className="font-semibold">Status:</span>{' '}
                  <span className={`font-semibold ${statusColor}`}>{statusLabel}</span>
                </p>
              </div>
            </div>
          </div>

          <div className="flex-shrink-0">
            <Button
                variant={buttonVariant}
                onClick={!isComplete ? onAssignAdmin : undefined}
                label={buttonLabel}
                className="w-auto"
                disabled={isComplete}
            />
          </div>
        </div>
      </div>
  );
};

WorkRequestCard.propTypes = {
  company: PropTypes.shape({ name: PropTypes.string }),
  teamName: PropTypes.string.isRequired,
  description: PropTypes.string,
  state: PropTypes.string.isRequired,
  responsibleAdminName: PropTypes.string,
  startDate: PropTypes.oneOfType([PropTypes.string, PropTypes.instanceOf(Date)]),
  endDate: PropTypes.oneOfType([PropTypes.string, PropTypes.instanceOf(Date)]),
  createdAt: PropTypes.oneOfType([PropTypes.string, PropTypes.instanceOf(Date)]),
  onAssignAdmin: PropTypes.func.isRequired,
};

WorkRequestCard.defaultProps = {
  description: 'N/A',
  responsibleAdminName: null,
  startDate: null,
  endDate: null,
  createdAt: null,
  company: null,
};

export default WorkRequestCard;
