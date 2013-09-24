/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
/**
 * Generated by Gas3 v1.1.0 (Granite Data Services) on Sat Jul 26 17:58:20 CEST 2008.
 *
 * WARNING: DO NOT CHANGE THIS FILE. IT MAY BE OVERRIDDEN EACH TIME YOU USE
 * THE GENERATOR. CHANGE INSTEAD THE INHERITED CLASS (Person.as).
 */

package org.granite.test.tide.data {

    import flash.utils.IDataInput;
    import flash.utils.IDataOutput;
    
    import mx.collections.ListCollectionView;
    
    import org.granite.meta;
    import org.granite.test.tide.AbstractEntity;
    import org.granite.tide.IEntityManager;
    import org.granite.tide.IPropertyHolder;

    use namespace meta;

    [Managed]
    [RemoteClass(alias="org.granite.test.tide.Prescription")]
    public class Prescription extends AbstractEntity {

		private var _fills:ListCollectionView;
        private var _name:String;
		private var _medication:Medication;
        
        
		public function set fills(value:ListCollectionView):void {
			_fills = value;
		}
		public function get fills():ListCollectionView {
			return _fills;
		}
		
		public function set name(value:String):void {
			_name = value;
		}
		public function get name():String {
			return _name;
		}
		
		public function set medication(value:Medication):void {
			_medication = value;
		}
		public function get medication():Medication {
			return _medication;			
		}

        override meta function merge(em:IEntityManager, obj:*):void {
            var src:Prescription = Prescription(obj);
            super.meta::merge(em, obj);
            if (meta::isInitialized()) {
				em.meta_mergeExternal(src._fills, _fills, null, this, 'fills', function setter(o:*):void{_fills = o as ListCollectionView}) as ListCollectionView;
				em.meta_mergeExternal(src._medication, _medication, null, this, 'medication', function setter(o:*):void{_medication = o as Medication}) as Medication;
                em.meta_mergeExternal(src._name, _name, null, this, 'name', function setter(o:*):void{_name = o as String}) as String;
            }
        }

        override public function readExternal(input:IDataInput):void {
            super.readExternal(input);
            if (meta::isInitialized()) {
				_fills = input.readObject() as ListCollectionView;
				_medication = input.readObject() as Medication;
                _name = input.readObject() as String;
            }
        }

        override public function writeExternal(output:IDataOutput):void {
            super.writeExternal(output);
            if (meta::isInitialized()) {
				output.writeObject((_fills is IPropertyHolder) ? IPropertyHolder(_fills).object : _fills);
				output.writeObject((_medication is IPropertyHolder) ? IPropertyHolder(_medication).object : _medication);
                output.writeObject((_name is IPropertyHolder) ? IPropertyHolder(_name).object : _name);
            }
        }
    }
}
