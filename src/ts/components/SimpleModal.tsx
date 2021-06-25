import React from "react";
import {Button, Modal, ModalBody, ModalFooter, ModalHeader, ModalProps} from "reactstrap";
import {SimpleErrorBoundary} from "./SimpleErrorBoundary";

export type SimpleModalProps = React.PropsWithChildren<{
    title: string,
    submitLabel: string,
    isOpen: boolean,
    closeModal: () => void,
    onSubmit: React.MouseEventHandler<HTMLElement>,
    size?: string
}>;

export const SimpleModal: React.FC<SimpleModalProps> = (
    {title, submitLabel, isOpen, closeModal, onSubmit, size, children}
) => {
    return <SimpleErrorBoundary context={`a modal dialog: '${title}'`}>
        <Modal isOpen={isOpen} toggle={closeModal} backdrop='static' size={size}>
            <ModalHeader toggle={closeModal}>
                {title}
            </ModalHeader>
            <ModalBody className="p-3">
                {children}
            </ModalBody>
            <ModalFooter>
                <Button color="secondary" onClick={e => {
                    e.preventDefault();
                    closeModal();
                }}>Cancel</Button>
                <Button color="primary" onClick={e => {
                    e.preventDefault();
                    closeModal();
                    onSubmit(e);
                }}>{submitLabel}</Button>
            </ModalFooter>
        </Modal>
    </SimpleErrorBoundary>;
};