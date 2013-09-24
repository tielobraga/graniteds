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
    
    import org.granite.collections.IMap;
    import org.granite.meta;
    import org.granite.ns.tide;
    import org.granite.tide.IEntityManager;
    import org.granite.tide.IPropertyHolder;
    import org.granite.util.Enum;
	import org.granite.test.tide.AbstractEntity;

    use namespace meta;
    use namespace tide;

    [Managed]
    [RemoteClass(alias="org.granite.test.tide.data.Person9")]
    public class Person9 extends AbstractEntity {

		private var _salutation:Salutation;
        private var _contacts:ListCollectionView;
        private var _firstName:String;
		private var _testMap:IMap;
        private var _lastName:String;
        
        
        public function set salutation(value:Salutation):void {
        	_salutation = value;
        }
        public function get salutation():Salutation {
        	return _salutation;
        }
        
        public function set contacts(value:ListCollectionView):void {
            _contacts = value;
        }
        public function get contacts():ListCollectionView {
            return _contacts;
        }

        public function set firstName(value:String):void {
            _firstName = value;
        }
        public function get firstName():String {
            return _firstName;
        }
		
		public function set testMap(value:IMap):void {
			_testMap = value;
		}
		public function get testMap():IMap {
			return _testMap;
		}

        public function set lastName(value:String):void {
            _lastName = value;
        }
        public function get lastName():String {
            return _lastName;
        }

        override meta function merge(em:IEntityManager, obj:*):void {
            var src:Person9 = Person9(obj);
            super.meta::merge(em, obj);
            if (meta::isInitialized()) {
                em.meta_mergeExternal(src._contacts, _contacts, null, this, 'contacts', function setter(o:*):void{_contacts = o as ListCollectionView}) as ListCollectionView;
                em.meta_mergeExternal(src._firstName, _firstName, null, this, 'firstName', function setter(o:*):void{_firstName = o as String}) as String;
                em.meta_mergeExternal(src._lastName, _lastName, null, this, 'lastName', function setter(o:*):void{_lastName = o as String}) as String;
                em.meta_mergeExternal(src._salutation, _salutation, null, this, 'salutation', function setter(o:*):void{_salutation = o as Salutation}) as Salutation;
				em.meta_mergeExternal(src._testMap, _testMap, null, this, 'testMap', function setter(o:*):void{_testMap = o as IMap}) as IMap;
            }
        }

        override public function readExternal(input:IDataInput):void {
            super.readExternal(input);
            if (meta::isInitialized()) {
                _contacts = input.readObject() as ListCollectionView;
                _firstName = input.readObject() as String;
                _lastName = input.readObject() as String;
                _salutation = Enum.readEnum(input) as Salutation;
				_testMap = input.readObject() as IMap;
            }
        }

        override public function writeExternal(output:IDataOutput):void {
            super.writeExternal(output);
            if (meta::isInitialized()) {
                output.writeObject((_contacts is IPropertyHolder) ? IPropertyHolder(_contacts).object : _contacts);
                output.writeObject((_firstName is IPropertyHolder) ? IPropertyHolder(_firstName).object : _firstName);
                output.writeObject((_lastName is IPropertyHolder) ? IPropertyHolder(_lastName).object : _lastName);
                output.writeObject((_salutation is IPropertyHolder) ? IPropertyHolder(_salutation).object : _salutation);
				output.writeObject((_testMap is IPropertyHolder) ? IPropertyHolder(_testMap).object : _testMap);
            }
        }
    }
}
