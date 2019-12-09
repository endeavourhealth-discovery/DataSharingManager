import {Component, ElementRef, HostBinding, Input, OnDestroy, OnInit, Optional, Self} from '@angular/core';
import {ConceptDefinition} from '../../models/ConceptDefinition';
import {MatFormFieldControl} from '@angular/material/form-field';
import {Subject} from 'rxjs';
import {NgControl} from '@angular/forms';
import {FocusMonitor} from '@angular/cdk/a11y';
import {coerceBooleanProperty} from '@angular/cdk/coercion';

@Component({
  selector: 'concept-definition',
  templateUrl: './concept-definition.component.html',
  styleUrls: ['./concept-definition.component.scss'],
  providers: [{provide: MatFormFieldControl, useExisting: ConceptDefinitionComponent}]
})
export class ConceptDefinitionComponent implements OnDestroy, MatFormFieldControl<ConceptDefinition> {
  static nextId = 0;

  @HostBinding() id = `concept-definition-${ConceptDefinitionComponent.nextId++}`;
  stateChanges = new Subject<void>();
  private _placeholder: string;
  private _required = false;
  private _disabled = false;
  definition: ConceptDefinition;
  focused = false;
  errorState = false;
  controlType = 'concept-definition';

  @Input()
  get value(): ConceptDefinition | null {
    return this.definition;
  }
  set value(definition: ConceptDefinition) {
    this.definition = definition;
    this.stateChanges.next();
  }

  @Input()
  get placeholder() {
    return this._placeholder;
  }
  set placeholder(plh) {
    this._placeholder = plh;
    this.stateChanges.next();
  }

  get empty() {
    return this.definition == null;
  }

  @HostBinding('class.floating')
  get shouldLabelFloat() {
    return this.focused || !this.empty;
  }

  @Input()
  get required() {
    return this._required;
  }
  set required(req) {
    this._required = coerceBooleanProperty(req);
    this.stateChanges.next();
  }

  @Input()
  get disabled(): boolean { return this._disabled; }
  set disabled(value: boolean) {
    this._disabled = coerceBooleanProperty(value);
    this.stateChanges.next();
  }

  @HostBinding('attr.aria-describedby') describedBy = '';

  setDescribedByIds(ids: string[]) {
    this.describedBy = ids.join(' ');
  }

  onContainerClick(event: MouseEvent) {
  }
  constructor(@Optional() @Self() public ngControl: NgControl,
              private fm: FocusMonitor,
              private elRef: ElementRef<HTMLElement>) {
    fm.monitor(elRef.nativeElement, true).subscribe(origin => {
      this.focused = !!origin;
      this.stateChanges.next();
    });
  }

  ngOnDestroy() {
    this.stateChanges.complete();
    this.fm.stopMonitoring(this.elRef.nativeElement);
  }

}
